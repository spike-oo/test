package com.campus.delivery.modules.ai.service;

import com.campus.delivery.common.enums.OrderStatus;
import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.order.dto.OrderVO;
import com.campus.delivery.modules.order.service.OrderService;
import com.campus.delivery.modules.user.entity.DietProfile;
import com.campus.delivery.modules.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 饮食分析。主责：成员1（需求文档 5.5.5）。
 *
 * <p>数据来源：学生的历史已完成订单明细 + 饮食档案。
 * 统计口径由后端计算（订单数、菜品构成、关键词命中），模型只负责生成健康建议与过敏原提醒，
 * 避免模型编造营养数据。
 *
 * <p>营养估算为简化版：按菜品标签中的关键词做定性判断，不声称精确热量。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietAnalysisService {

    private static final int ORDER_LIMIT = 50;

    /** 饮食结构关键词分组（命中即计数） */
    private static final Map<String, List<String>> STRUCTURE_KEYWORDS = Map.of(
            "HIGH_OIL", List.of("油炸", "炸", "油", "红烧", "重口"),
            "HIGH_SALT", List.of("腌制", "咸", "重口", "卤"),
            "LIGHT", List.of("清淡", "少油", "低脂", "水煮", "蒸"),
            "VEGETABLE", List.of("蔬菜", "青菜", "时蔬", "沙拉", "素"),
            "MEAT", List.of("肉", "鸡", "牛", "猪", "鱼", "虾", "蛋"),
            "SPICY", List.of("辣", "麻辣", "香辣")
    );

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final OrderService orderService;
    private final DishService dishService;
    private final UserService userService;

    /**
     * 生成饮食分析报告。
     *
     * @param days 统计周期：7 近 7 天 / 30 近 30 天
     */
    public Map<String, Object> report(String userId, Integer days) {
        int period = (days == null || days <= 0) ? 7 : days;
        DietProfile profile = userService.getOrCreateDietProfile(userId);
        List<OrderVO> orders = orderService.pageMyOrders(userId, OrderStatus.FINISHED.getCode(), 1, ORDER_LIMIT).getList();

        // 1) 后端统计：菜品构成与关键词命中
        Map<String, Integer> structure = new LinkedHashMap<>();
        STRUCTURE_KEYWORDS.keySet().forEach(key -> structure.put(key, 0));
        List<String> dishNames = new ArrayList<>();
        List<String> allergyHits = new ArrayList<>();
        List<String> banned = splitTokens(profile.getAllergyFoods());

        int dishCount = 0;
        for (OrderVO vo : orders) {
            vo.getItems().forEach(item -> {
                dishNames.add(item.getDishName());
                Dish dish = safeGetDish(item.getDishId());
                String haystack = item.getDishName()
                        + (dish == null ? "" : "," + nullSafe(dish.getTags()) + "," + nullSafe(dish.getDescription()));
                STRUCTURE_KEYWORDS.forEach((key, keywords) -> {
                    if (keywords.stream().anyMatch(haystack::contains)) {
                        structure.merge(key, 1, Integer::sum);
                    }
                });
                if (banned.stream().anyMatch(haystack::contains)) {
                    allergyHits.add(item.getDishName());
                }
            });
            dishCount += vo.getItems().size();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("periodDays", period);
        result.put("orderCount", orders.size());
        result.put("dishCount", dishCount);
        result.put("structure", structure);
        result.put("allergyRisk", allergyHits.stream().distinct().toList());

        if (orders.isEmpty()) {
            result.put("message", "近 " + period + " 天暂无已完成订单，无法生成饮食分析");
            result.put("suggestion", "先完成一次点餐，系统会自动积累饮食数据");
            return result;
        }

        // 2) 模型生成个性化建议
        if (llmClient.available()) {
            Map<String, String> variables = new HashMap<>(4);
            variables.put("dietProfile", describeProfile(profile));
            variables.put("statistics", describeStatistics(result));
            variables.put("dishNames", String.join("、", dishNames.stream().distinct().limit(30).toList()));

            String[] prompts = promptTemplates.systemAndUser("diet-analysis", variables);
            JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);
            if (json != null) {
                List<String> suggestions = new ArrayList<>();
                json.path("suggestions").forEach(node -> suggestions.add(node.asText()));
                result.put("summary", json.path("summary").asText(""));
                result.put("suggestions", suggestions);
                result.put("modelVersion", llmClient.modelVersion());
                // TODO(成员1)：报告落库 diet_report（含统计区间、结构数据、营养数据、建议、模型版本）
                return result;
            }
        }

        // 3) 降级：规则化建议
        result.put("summary", "根据近 " + period + " 天的订单统计生成（AI 建议暂不可用）");
        result.put("suggestions", ruleBasedSuggestions(structure, allergyHits, profile));
        return result;
    }

    // ------------------------------------------------------------------

    private List<String> ruleBasedSuggestions(Map<String, Integer> structure,
                                             List<String> allergyHits, DietProfile profile) {
        List<String> suggestions = new ArrayList<>();
        if (structure.getOrDefault("HIGH_OIL", 0) > structure.getOrDefault("LIGHT", 0)) {
            suggestions.add("近期高油菜品占比偏高，建议增加清蒸、水煮类菜品");
        }
        if (structure.getOrDefault("VEGETABLE", 0) < structure.getOrDefault("MEAT", 0)) {
            suggestions.add("蔬菜类摄入少于肉类，建议每餐搭配一份青菜或时蔬");
        }
        if (structure.getOrDefault("SPICY", 0) > 3) {
            suggestions.add("近期偏辣菜品较多，肠胃敏感时可适当减少");
        }
        if (!allergyHits.isEmpty()) {
            suggestions.add("近期的 " + String.join("、", allergyHits.stream().distinct().toList())
                    + " 可能含有你的忌口/过敏原食材，请留意");
        }
        Integer goal = profile == null ? null : profile.getDietGoal();
        if (goal != null && goal == 1) {
            suggestions.add("当前饮食目标为减脂，建议优先选择低脂、高蛋白的菜品");
        } else if (goal != null && goal == 2) {
            suggestions.add("当前饮食目标为增肌，建议保证每餐有足量蛋白质摄入");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("近期饮食结构较为均衡，请继续保持");
        }
        return suggestions;
    }

    private String describeStatistics(Map<String, Object> result) {
        return "统计周期：近 " + result.get("periodDays") + " 天；"
                + "订单数：" + result.get("orderCount") + "；"
                + "菜品份数：" + result.get("dishCount") + "；"
                + "结构关键词命中：" + result.get("structure") + "；"
                + "疑似过敏原命中：" + result.get("allergyRisk");
    }

    private String describeProfile(DietProfile profile) {
        if (profile == null) {
            return "（无饮食档案）";
        }
        return "饮食目标：" + profile.getDietGoal()
                + "；口味偏好：" + nullSafe(profile.getTastePreference())
                + "；忌口/过敏原：" + nullSafe(profile.getAllergyFoods());
    }

    private Dish safeGetDish(String dishId) {
        try {
            return dishService.getDetail(dishId);
        } catch (Exception e) {
            log.debug("菜品查询失败，跳过标签统计: dishId={}", dishId);
            return null;
        }
    }

    private List<String> splitTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return Arrays.stream(text.split("[,，;；、\\s]+"))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private String nullSafe(String text) {
        return text == null ? "" : text;
    }
}
