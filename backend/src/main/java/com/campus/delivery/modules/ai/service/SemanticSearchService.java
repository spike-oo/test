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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 语义搜索。主责：成员1。
 *
 * <p>支持口语化、模糊化描述（如「适合减脂的」「不上火的」），
 * 模型负责理解语义并输出菜品ID，菜品信息一律回查数据库。
 * 失败时降级为关键词检索（需求文档 5.6.3）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private static final int CANDIDATE_LIMIT = 60;
    private static final int RESULT_LIMIT = 10;

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final DishService dishService;
    private final UserService userService;
    private final NlOrderService nlOrderService;

    /** 语义搜索 */
    public AiOrderResponse search(String userId, AiChatRequest request) {
        AiOrderResponse response = new AiOrderResponse();
        response.setModelVersion(llmClient.modelVersion());
        response.setSessionId(request.getSessionId());

        DietProfile profile = userService.getOrCreateDietProfile(userId);
        List<Dish> candidates = dishService.listOnShelfDishes(request.getShopId(), CANDIDATE_LIMIT);

        if (llmClient.available() && !candidates.isEmpty()) {
            try {
                Map<String, String> variables = new HashMap<>(4);
                variables.put("dietProfile", nlOrderService.describeProfile(profile));
                variables.put("candidates", nlOrderService.describeDishes(candidates));
                variables.put("message", request.getMessage());

                String[] prompts = promptTemplates.systemAndUser("semantic-search", variables);
                JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);
                if (json != null) {
                    response.setReply(json.path("reply").asText(""));
                    List<String> dishIds = new ArrayList<>();
                    for (JsonNode node : json.path("dishIds")) {
                        dishIds.add(node.asText());
                    }
                    List<Dish> matched = nlOrderService.filterAllergens(
                            dishService.listByIds(dishIds), profile);
                    if (!matched.isEmpty()) {
                        response.setDishes(matched);
                        response.setFallback(false);
                        return response;
                    }
                }
            } catch (BusinessException e) {
                log.warn("语义搜索调用失败，降级为关键词检索: {}", e.getMessage());
            }
        }

        response.setFallback(true);
        response.setReply("已按关键词为你匹配以下菜品");
        response.setDishes(nlOrderService.filterAllergens(
                dishService.searchByKeyword(request.getMessage(), request.getShopId(), RESULT_LIMIT), profile));
        return response;
    }
}
