package com.campus.delivery.modules.ai.service;

import com.campus.delivery.common.constant.RedisKeys;
import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.user.entity.DietProfile;
import com.campus.delivery.modules.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 个性化推荐。主责：成员1（需求文档 5.6.5 难点五）。
 *
 * <p>两阶段策略：
 * <ol>
 *   <li><b>规则召回</b>：按饮食档案过滤忌口/过敏原，按月销量与时段取候选集；</li>
 *   <li><b>模型重排</b>：把候选集与画像交给大模型，输出排序后的菜品ID与可解释的推荐理由。</li>
 * </ol>
 *
 * <p>冷启动：新用户没有历史订单，直接用「校园热门菜品 + 画像规则过滤」生成推荐；
 * 模型不可用时同样退化为规则推荐，保证「新老用户均有推荐」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private static final int CANDIDATE_LIMIT = 40;
    private static final int RESULT_LIMIT = 6;
    private static final long CACHE_MINUTES = 10L;

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final DishService dishService;
    private final UserService userService;
    private final NlOrderService nlOrderService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 个性化推荐 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> recommend(String userId, String scene, Integer limit) {
        int size = limit == null ? RESULT_LIMIT : limit;
        String cacheKey = RedisKeys.AI_RECOMMEND + userId + ":" + scene;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Map) {
            return (Map<String, Object>) cached;
        }

        DietProfile profile = userService.getOrCreateDietProfile(userId);
        List<Dish> candidates = dishService.listOnShelfDishes(null, CANDIDATE_LIMIT);
        List<Dish> filtered = nlOrderService.filterAllergens(candidates, profile);

        List<Dish> recommended = null;
        String reason = null;

        if (llmClient.available() && !filtered.isEmpty()) {
            try {
                Map<String, String> variables = new HashMap<>(4);
                variables.put("dietProfile", nlOrderService.describeProfile(profile));
                variables.put("candidates", nlOrderService.describeDishes(filtered));
                variables.put("scene", sceneText(scene));
                variables.put("limit", String.valueOf(size));

                String[] prompts = promptTemplates.systemAndUser("recommend-reason", variables);
                JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);
                if (json != null) {
                    List<String> dishIds = new ArrayList<>();
                    for (JsonNode node : json.path("dishIds")) {
                        dishIds.add(node.asText());
                    }
                    List<Dish> matched = nlOrderService.filterAllergens(dishService.listByIds(dishIds), profile);
                    if (!matched.isEmpty()) {
                        recommended = matched;
                        reason = json.path("reason").asText("");
                    }
                }
            } catch (Exception e) {
                log.warn("推荐重排失败，使用规则推荐: {}", e.getMessage());
            }
        }

        boolean coldStart = false;
        if (recommended == null || recommended.isEmpty()) {
            // 冷启动 / 降级：热门菜品 + 画像过滤（时段感知）
            recommended = filtered.size() > size ? filtered.subList(0, size) : filtered;
            coldStart = true;
            reason = "根据校园热门菜品与你的饮食档案为你推荐";
        }

        Map<String, Object> result = new HashMap<>(6);
        result.put("dishes", recommended);
        result.put("reason", reason);
        result.put("coldStart", coldStart);
        result.put("scene", scene);
        result.put("modelVersion", llmClient.modelVersion());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_MINUTES, TimeUnit.MINUTES);
        // TODO(成员1)：把推荐结果写入 recommend_record（含输入、解析条件、菜品ID、模型版本、校验状态）
        return result;
    }

    /** 时段感知：早/午/晚/夜宵 */
    public String currentTimeSlot() {
        int hour = LocalTime.now().getHour();
        if (hour < 10) {
            return "BREAKFAST";
        }
        if (hour < 14) {
            return "LUNCH";
        }
        if (hour < 20) {
            return "DINNER";
        }
        return "NIGHT_SNACK";
    }

    private String sceneText(String scene) {
        if (scene == null) {
            return "首页推荐";
        }
        switch (scene) {
            case "AI_ORDER":
                return "AI 点餐结果页推荐";
            case "SEARCH":
                return "搜索结果页推荐";
            default:
                return "首页个性化推荐（当前时段：" + currentTimeSlot() + "）";
        }
    }
}
