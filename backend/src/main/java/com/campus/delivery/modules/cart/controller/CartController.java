package com.campus.delivery.modules.cart.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.cart.dto.AddCartRequest;
import com.campus.delivery.modules.cart.dto.CartItemVO;
import com.campus.delivery.modules.cart.service.CartService;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 购物车接口。主责：成员1。
 */
@Tag(name = "07-购物车", description = "加入购物车、数量调整、勾选、删除")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.STUDENT)
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.listCart(UserContext.getUserId()));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<Void> add(@RequestBody AddCartRequest request) {
        cartService.addToCart(UserContext.getUserId(), request);
        return Result.ok();
    }

    @Operation(summary = "调整数量")
    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable String id, @RequestParam Integer quantity) {
        cartService.updateQuantity(UserContext.getUserId(), id, quantity);
        return Result.ok();
    }

    @Operation(summary = "勾选或取消勾选")
    @PutMapping("/{id}/selected")
    public Result<Void> updateSelected(@PathVariable String id, @RequestParam Integer selected) {
        cartService.updateSelected(UserContext.getUserId(), id, selected);
        return Result.ok();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        cartService.remove(UserContext.getUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.ok();
    }
}
