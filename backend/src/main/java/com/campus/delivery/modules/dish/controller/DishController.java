package com.campus.delivery.modules.dish.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.entity.DishCategory;
import com.campus.delivery.modules.dish.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜品浏览接口（公开）。主责：成员2，使用方：成员1（学生端）。
 */
@Tag(name = "05-菜品（公开）", description = "菜品列表、详情、关键词检索")
@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @Operation(summary = "菜品详情")
    @GetMapping("/{id}")
    public Result<Dish> detail(@PathVariable String id) {
        return Result.ok(dishService.getDetail(id));
    }

    @Operation(summary = "菜品关键词检索", description = "AI 语义搜索的降级兜底方案")
    @GetMapping("/search")
    public Result<List<Dish>> search(@RequestParam String keyword,
                                     @RequestParam(required = false) String shopId,
                                     @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(dishService.searchByKeyword(keyword, shopId, limit));
    }
}
