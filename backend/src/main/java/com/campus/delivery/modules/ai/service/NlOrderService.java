package com.campus.delivery.modules.ai.service;

import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.dto.AiChatRequest;
import com.campus.delivery.modules.ai.dto.AiOrderResponse;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.user.entity.DietProfile;
import com.campus.delivery.modules.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 自然语言点餐。主责：成员1。
 *
 * <p>流程（需求文档 4.3 时序）：
 * <pre>
 *   学生输入自然语言 → 取饮食档案 → 组候选菜品 → 调用大模型解析意图与实体
 *   → 回查菜品 → 忌口/过敏原二次过滤 → 生成推荐理由 → 返回候选列表
 * </pre>
 *
 * <p>关键设计（需求文档 5.6.3 / 5.6.5）：
 * <ol>
 *   <li>业务数据以结构化方式注入提示词，不让模型自行猜测菜品；</li>
 *   <li>模型只返回菜品ID，菜品信息一律回查数据库，避免模型编造；</li>
 *   <li>模型调用或 JSON 解析失败时降级为关键词检索，不把异常抛给前端。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NlOrderService {

    private static final int CANDIDATE_LIMIT = 60;
    private static final int RESULT_LIMIT = 6;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final DishService dishService;
    private final UserService userService;

    /** 解析自然语言需求并推荐菜品 */
    public AiOrderResponse parseAndMatch(String userId, AiChatRequest request) {
        AiOrderResponse response = new AiOrderResponse();
        response.setModelVersion(llmClient.modelVersion());
        response.setSessionId(request.getSessionId());

        DietProfile profile = userService.getOrCreateDietProfile(userId);
        List<Dish> candidates = dishService.listOnShelfDishes(request.getShopId(), CANDIDATE_LIMIT);

        if (llmClient.available() && !candidates.isEmpty()) {
            try {
                Map<String, String> variables = new HashMap<>(4);
                variables.put("dietProfile", describeProfile(profile));
                variables.put("candidates", describeDishes(candidates));
                variables.put("message", request.getMessage());

                String[] prompts = promptTemplates.systemAndUser("nl-order", variables);
                JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);

                if (json != null) {
                    response.setReply(json.path("reply").asText(""));
                    response.setConditions(toMap(json.path("conditions")));

                    List<String> dishIds = new ArrayList<>();
                    for (JsonNode node : json.path("dishIds")) {
                        dishIds.add(node.asText());
                    }

                    List<Dish> matched = filterAllergens(dishService.listByIds(dishIds), profile);
                    if (matched.isEmpty()) {
                        // 模型没有给出有效菜品：用候选集兜底，保证一定有结果
                        matched = fallbackByCandidates(candidates, profile);
                        response.setReply(response.getReply().isEmpty()
                                ? "没有完全符合的菜品，为你推荐几个相近的选择" : response.getReply());
                    }
                    response.setDishes(limit(matched));
                    response.setFallback(false);
                    return response;
                }
            } catch (BusinessException e) {
                log.warn("AI 点餐调用失败，降级为关键词检索: {}", e.getMessage());
            }
        }

        // 降级：关键词检索兜底
        response.setFallback(true);
        response.setReply("AI 理解暂时不可用，已按关键词为你推荐以下菜品");
        List<Dish> keywordResult = dishService.searchByKeyword(request.getMessage(), request.getShopId(), RESULT_LIMIT);
        if (keywordResult.isEmpty()) {
            keywordResult = fallbackByCandidates(candidates, profile);
        }
        response.setDishes(limit(filterAllergens(keywordResult, profile)));
        return response;
    }

    // ------------------------------------------------------------------
    // 供 RecommendService 复用的工具方法
    // ------------------------------------------------------------------

    /** 把饮食档案转成提示词可读的文本 */
    public String describeProfile(DietProfile profile) {
        if (profile == null) {
            return "（无饮食档案）";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("饮食目标：").append(goalText(profile.getDietGoal())).append("；");
        sb.append("口味偏好：").append(textOrNone(profile.getTastePreference())).append("；");
        sb.append("忌口/过敏原：").append(textOrNone(profile.getAllergyFoods())).append("；");
        sb.append("不喜欢的食材：").append(textOrNone(profile.getDislikeFoods()));
        return sb.toString();
    }

    /** 把菜品列表转成提示词可读的文本（只给ID与客观字段，避免模型编造） */
    public String describeDishes(List<Dish> dishes) {
        StringBuilder sb = new StringBuilder();
        for (Dish dish : dishes) {
            sb.append("- id=").append(dish.getId())
                    .append(" | 名称=").append(dish.getDishName())
                    .append(" | 价格=").append(dish.getPrice())
                    .append(" | 标签=").append(textOrNone(dish.getTags()))
                    .append(" | 食材=").append(textOrNone(dish.getIngredients()))
                    .append(" | 月销量=").append(dish.getMonthlySales())
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * 忌口/过敏原二次过滤。
     *
     * <p>不信任模型结果：即使模型把含过敏原的菜品排进结果，这里也会剔除。
     */
    public List<Dish> filterAllergens(List<Dish> dishes, DietProfile profile) {
        if (dishes == null || dishes.isEmpty() || profile == null) {
            return dishes == null ? List.of() : dishes;
        }
        List<String> banned = splitTokens(profile.getAllergyFoods());
        banned.addAll(splitTokens(profile.getDislikeFoods()));
        if (banned.isEmpty()) {
            return dishes;
        }
        List<Dish> result = new ArrayList<>(dishes.size());
        for (Dish dish : dishes) {
            String haystack = textOrNone(dish.getIngredients()) + "," + textOrNone(dish.getTags())
                    + "," + textOrNone(dish.getDescription());
            boolean hit = banned.stream().anyMatch(haystack::contains);
            if (!hit) {
                result.add(dish);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------

    /** 无历史数据或模型无结果时：按销量取候选集（冷启动策略） */
    private List<Dish> fallbackByCandidates(List<Dish> candidates, DietProfile profile) {
        return filterAllergens(candidates, profile);
    }

    private List<Dish> limit(List<Dish> dishes) {
        return dishes.size() <= RESULT_LIMIT ? dishes : dishes.subList(0, RESULT_LIMIT);
    }

    private List<String> splitTokens(String text) {
        List<String> tokens = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return tokens;
        }
        for (String token : text.split("[,，;；、\\s]+")) {
            if (StringUtils.hasText(token)) {
                tokens.add(token.trim());
            }
        }
        return tokens;
    }

    private Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return map;
        }
        node.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
        return map;
    }

    private String goalText(Integer goal) {
        if (goal == null) {
            return "普通";
        }
        switch (goal) {
            case 1:
                return "减脂";
            case 2:
                return "增肌";
            default:
                return "普通";
        }
    }

    private String textOrNone(String text) {
        return StringUtils.hasText(text) ? text : "无";
    }
}
