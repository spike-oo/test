package com.campus.delivery.modules.review.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.review.dto.ReviewSubmitDTO;
import com.campus.delivery.modules.review.entity.Review;
import com.campus.delivery.modules.review.service.ReviewService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价接口（学生提交 + 公开查看）。主责：成员2。
 */
@Tag(name = "11-评价", description = "提交评价、查看评价")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "提交评价", description = "仅已完成的订单可评价")
    @PostMapping
    @RequiresRole(RoleEnum.STUDENT)
    public Result<Review> submit(@RequestBody ReviewSubmitDTO dto) {
        return Result.ok(reviewService.submit(UserContext.getUserId(), dto));
    }

    @Operation(summary = "店铺评价列表")
    @GetMapping
    public Result<PageResult<Review>> list(@RequestParam String shopId,
                                           @RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(reviewService.pageByShop(shopId, pageNum, pageSize));
    }
}
