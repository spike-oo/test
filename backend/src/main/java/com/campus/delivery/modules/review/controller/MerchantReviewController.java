package com.campus.delivery.modules.review.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.review.entity.Review;
import com.campus.delivery.modules.review.service.ReviewService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商户端评价管理接口。主责：成员2。
 */
@Tag(name = "12-商户端·评价管理", description = "评价查看、回复、标签聚合、评分概览")
@RestController
@RequestMapping("/merchant/reviews")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.MERCHANT)
public class MerchantReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "本店评价列表", description = "可按情感倾向筛选：0差评 1中评 2好评")
    @GetMapping
    public Result<PageResult<Review>> list(@RequestParam(required = false) Integer sentiment,
                                           @RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(reviewService.pageMyShopReviews(sentiment, pageNum, pageSize));
    }

    @Operation(summary = "回复评价")
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable String id, @RequestParam String content) {
        reviewService.reply(id, content);
        return Result.ok();
    }

    @Operation(summary = "评价标签聚合", description = "标签云、好评点排行、吐槽点排行的数据源")
    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> tags(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(reviewService.statTags(limit));
    }

    @Operation(summary = "评价概览", description = "评价总数、各项平均分、好评率")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(reviewService.statOverview());
    }
}
