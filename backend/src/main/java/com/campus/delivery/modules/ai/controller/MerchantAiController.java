package com.campus.delivery.modules.ai.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.ai.service.BusinessAnalysisService;
import com.campus.delivery.modules.ai.service.ReviewAnalysisService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 能力接口（商户端）。主责：成员2。
 */
@Tag(name = "11-AI·商户端", description = "评价分析、评价报告、经营简报")
@RestController
@RequestMapping("/merchant/ai")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.MERCHANT)
public class MerchantAiController {

    private final ReviewAnalysisService reviewAnalysisService;
    private final BusinessAnalysisService businessAnalysisService;

    @Operation(summary = "重新分析指定评价", description = "标签提取 + 情感分析，结果回写 review / review_tag")
    @PostMapping("/reviews/{reviewId}/analyze")
    public Result<Map<String, Object>> analyzeReview(@PathVariable String reviewId) {
        return Result.ok(reviewAnalysisService.analyze(reviewId));
    }

    @Operation(summary = "评价分析报告", description = "标签词频聚合 + 好评点/吐槽点 + 改进建议")
    @GetMapping("/review-report")
    public Result<Map<String, Object>> reviewReport() {
        return Result.ok(reviewAnalysisService.report());
    }

    @Operation(summary = "AI 经营简报", description = "热销/滞销菜品 + 评价概览 + 经营建议")
    @GetMapping("/business-report")
    public Result<Map<String, Object>> businessReport() {
        return Result.ok(businessAnalysisService.report());
    }
}
