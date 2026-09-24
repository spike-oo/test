package com.campus.delivery.modules.shop.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.shop.entity.Shop;
import com.campus.delivery.modules.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺浏览接口（公开）。主责：成员2，使用方：成员1（学生端首页）。
 */
@Tag(name = "03-店铺（公开）", description = "店铺列表与详情")
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "店铺列表", description = "支持分类筛选、名称搜索，按销量或评分排序")
    @GetMapping
    public Result<PageResult<Shop>> list(@RequestParam(required = false) String categoryId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false, defaultValue = "sales") String sortBy,
                                         @RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(shopService.pageShops(categoryId, keyword, sortBy, pageNum, pageSize));
    }

    @Operation(summary = "店铺详情")
    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable String id) {
        return Result.ok(shopService.getDetail(id));
    }
}
