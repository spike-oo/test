package com.campus.delivery.modules.ai.service;

import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.review.service.ReviewService;
import com.campus.delivery.modules.shop.service.ShopService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 经营辅助。主责：成员2（需求文档 5.2.5）。
 *
 * <p>能力：经营概览、热销/滞销菜品、评价标签与评分概览、AI 经营改进建议。
 * 统计数据全部由后端聚合，模型只做归纳与建议生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAnalysisService {

    private static final int DISH_LIMIT = 50;
    private static final int TOP_LIMIT = 10;

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final DishService dishService;
    private final ReviewService reviewService;
    private final ShopService shopService;

    /** 经营简报 */
    public Map<String, Object> report() {
        String shopId = shopService.currentShopId();
        List<Dish> dishes = dishService.listOnShelfDishes(shopId, DISH_LIMIT);

        // 热销：销量 Top10；滞销：销量尾部（销量低、需关注）
        List<Dish> hot = dishes.stream().limit(TOP_LIMIT).toList();
        List<Dish> slow = dishes.size() <= TOP_LIMIT ? List.of()
                : dishes.subList(Math.max(0, dishes.size() - TOP_LIMIT), dishes.size());

        Map<String, Object> overview = reviewService.statOverview();
        List<Map<String, Object>> tags = reviewService.statTags(TOP_LIMIT);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shopId", shopId);
        result.put("dishCount", dishes.size());
        result.put("hotDishes", hot.stream().map(Dish::getDishName).toList());
        result.put("slowDishes", slow.stream().map(Dish::getDishName).toList());
        result.put("reviewOverview", overview);
        result.put("reviewTags", tags);

        if (!llmClient.available()) {
            result.put("message", "大模型能力未启用，仅返回统计数据");
            return result;
        }

        Map<String, String> variables = new HashMap<>(4);
        variables.put("hotDishes", describeDishes(hot));
        variables.put("slowDishes", describeDishes(slow));
        variables.put("reviewOverview", String.valueOf(overview));
        variables.put("reviewTags", String.valueOf(tags));

        String[] prompts = promptTemplates.systemAndUser("business-analysis", variables);
        JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);
        if (json != null) {
            List<String> suggestions = new ArrayList<>();
            json.path("suggestions").forEach(node -> suggestions.add(node.asText()));
            result.put("summary", json.path("summary").asText(""));
            result.put("suggestions", suggestions);
            result.put("modelVersion", llmClient.modelVersion());
        } else {
            result.put("message", "模型返回格式无法解析，仅返回统计数据");
        }
        return result;
    }

    private String describeDishes(List<Dish> dishes) {
        StringBuilder sb = new StringBuilder();
        for (Dish dish : dishes) {
            sb.append("- ").append(dish.getDishName())
                    .append("（月销量 ").append(dish.getMonthlySales())
                    .append("，评分 ").append(dish.getScore())
                    .append("，库存 ").append(dish.getStock()).append("）\n");
        }
        return sb.length() == 0 ? "（无数据）" : sb.toString();
    }
}
