package com.campus.delivery.modules.dish.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.entity.DishCategory;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.RequiresRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商户端菜品管理接口。主责：成员2。
 */
@Tag(name = "06-商户端·菜品管理", description = "菜品增删改查、上下架、库存、分类")
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.MERCHANT)
public class MerchantDishController {

    private final DishService dishService;

    @Operation(summary = "本店菜品列表")
    @GetMapping("/dishes")
    public Result<List<Dish>> list(@RequestParam(required = false) String categoryId,
                                   @RequestParam(required = false) Integer status) {
        return Result.ok(dishService.listMyDishes(categoryId, status));
    }

    @Operation(summary = "新增或修改菜品")
    @PostMapping("/dishes")
    public Result<Dish> save(@RequestBody Dish form) {
        return Result.ok(dishService.saveDish(form));
    }

    @Operation(summary = "菜品上下架", description = "0下架 1上架")
    @PutMapping("/dishes/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestParam Integer status) {
        dishService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "调整库存", description = "库存为 0 时自动下架")
    @PutMapping("/dishes/{id}/stock")
    public Result<Void> updateStock(@PathVariable String id, @RequestParam Integer stock) {
        dishService.updateStock(id, stock);
        return Result.ok();
    }

    @Operation(summary = "菜品分类列表")
    @GetMapping("/dish-categories")
    public Result<List<DishCategory>> categories(@RequestParam String shopId) {
        return Result.ok(dishService.listCategories(shopId));
    }
}
