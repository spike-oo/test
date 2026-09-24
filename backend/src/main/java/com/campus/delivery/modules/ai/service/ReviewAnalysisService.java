package com.campus.delivery.modules.ai.service;

import com.campus.delivery.common.enums.CommonEnums;
import com.campus.delivery.modules.ai.client.LlmClient;
import com.campus.delivery.modules.ai.prompt.PromptTemplates;
import com.campus.delivery.modules.review.dto.ReviewTagDTO;
import com.campus.delivery.modules.review.entity.Review;
import com.campus.delivery.modules.review.mapper.ReviewMapper;
import com.campus.delivery.modules.review.service.ReviewService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 评价分析。主责：成员2（需求文档 5.6.4 难点四）。
 *
 * <p>能力：
 * <ol>
 *   <li>单条评价：提取多维度标签（口味/分量/速度/卫生/服务/价格）与情感倾向；</li>
 *   <li>店铺报告：汇总标签做词频聚合，生成标签云、好评点/吐槽点排行与改进建议。</li>
 * </ol>
 *
 * <p>结果统一通过 {@link ReviewService#saveAiAnalysis} 回写，保证评价表只有成员2 的服务写入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    /** 允许的标签维度，模型返回其他维度时统一归为 OTHER */
    private static final List<String> ALLOWED_TYPES =
            List.of("TASTE", "PORTION", "SPEED", "HYGIENE", "SERVICE", "PRICE");

    private final LlmClient llmClient;
    private final PromptTemplates promptTemplates;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;

    /** 评价提交后异步分析，不阻塞学生端响应 */
    @Async
    public void analyzeAsync(String reviewId) {
        try {
            analyze(reviewId);
        } catch (Exception e) {
            log.error("评价 AI 分析失败: reviewId={}", reviewId, e);
        }
    }

    /** 分析单条评价：标签提取 + 情感分析 */
    public Map<String, Object> analyze(String reviewId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            result.put("success", false);
            result.put("message", "评价不存在");
            return result;
        }
        if (!StringUtils.hasText(review.getContent())) {
            result.put("success", false);
            result.put("message", "评价内容为空，跳过分析");
            return result;
        }
        if (!llmClient.available()) {
            result.put("success", false);
            result.put("message", "大模型能力未启用");
            return result;
        }

        Map<String, String> variables = new HashMap<>(2);
        variables.put("content", review.getContent());
        String[] prompts = promptTemplates.systemAndUser("review-analysis", variables);
        JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);

        if (json == null) {
            // 解析失败：不写入脏数据，等待重试
            result.put("success", false);
            result.put("message", "模型返回格式无法解析，已跳过本次分析");
            return result;
        }

        List<ReviewTagDTO> tags = new ArrayList<>();
        for (JsonNode node : json.path("tags")) {
            ReviewTagDTO tag = new ReviewTagDTO();
            tag.setTagName(node.path("tagName").asText(""));
            tag.setTagType(normalizeType(node.path("tagType").asText("")));
            tag.setSentiment(node.path("sentiment").asInt(CommonEnums.Sentiment.NEUTRAL.getCode()));
            if (StringUtils.hasText(tag.getTagName())) {
                tags.add(tag);
            }
        }

        Integer sentiment = json.path("sentiment").asInt(CommonEnums.Sentiment.NEUTRAL.getCode());
        String tagsCsv = tags.stream().map(ReviewTagDTO::getTagName).collect(Collectors.joining(","));

        reviewService.saveAiAnalysis(reviewId, tagsCsv, sentiment, llmClient.modelVersion(), tags);

        result.put("success", true);
        result.put("tags", tags);
        result.put("tagsCsv", tagsCsv);
        result.put("sentiment", sentiment);
        result.put("modelVersion", llmClient.modelVersion());
        return result;
    }

    /**
     * 店铺评价分析报告（商户端一键生成）。
     *
     * <p>数据来源：{@code review} 聚合统计 + {@code review_tag} 标签词频，
     * 模型只负责归纳与给出改进建议，统计数据一律由数据库聚合得出。
     */
    public Map<String, Object> report() {
        Map<String, Object> overview = reviewService.statOverview();
        List<Map<String, Object>> tags = reviewService.statTags(20);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("tags", tags);

        Long reviewCount = overview.get("reviewCount") == null ? 0L
                : Long.parseLong(String.valueOf(overview.get("reviewCount")));
        if (reviewCount == null || reviewCount < 5) {
            result.put("message", "评价数量不足，暂无法生成分析报告");
            result.put("suggestion", "建议先积累至少 5 条用户评价");
            return result;
        }
        if (!llmClient.available()) {
            result.put("message", "大模型能力未启用，仅返回统计数据");
            return result;
        }

        Map<String, String> variables = new HashMap<>(4);
        variables.put("overview", String.valueOf(overview));
        variables.put("tags", String.valueOf(tags));
        String[] prompts = promptTemplates.systemAndUser("review-report", variables);
        JsonNode json = llmClient.chatForJson(prompts[0], prompts[1]);

        if (json != null) {
            List<String> highlights = new ArrayList<>();
            json.path("highlights").forEach(node -> highlights.add(node.asText()));
            List<String> complaints = new ArrayList<>();
            json.path("complaints").forEach(node -> complaints.add(node.asText()));
            List<String> suggestions = new ArrayList<>();
            json.path("suggestions").forEach(node -> suggestions.add(node.asText()));

            result.put("summary", json.path("summary").asText(""));
            result.put("highlights", highlights);
            result.put("complaints", complaints);
            result.put("suggestions", suggestions);
            result.put("modelVersion", llmClient.modelVersion());
        } else {
            result.put("message", "模型返回格式无法解析，仅返回统计数据");
        }
        return result;
    }

    /** 统一维度取值，避免模型返回自由文本导致统计口径不一致 */
    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "OTHER";
        }
        String upper = type.trim().toUpperCase();
        return ALLOWED_TYPES.contains(upper) ? upper : "OTHER";
    }
}
