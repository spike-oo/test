package com.campus.delivery.modules.order.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.order.dto.OrderVO;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.service.OrderService;
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

/**
 * 商户端订单处理接口。主责：成员2（调用成员4 的状态机）。
 */
@Tag(name = "09-商户端·订单处理", description = "订单列表、接单、出餐、拒单")
@RestController
@RequestMapping("/merchant/orders")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.MERCHANT)
public class MerchantOrderController {

    private final OrderService orderService;

    @Operation(summary = "本店订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(orderService.pageShopOrders(status, pageNum, pageSize));
    }

    @Operation(summary = "接单", description = "订单状态变更为备餐中")
    @PostMapping("/{id}/accept")
    public Result<Order> accept(@PathVariable String id) {
        return Result.ok(orderService.merchantTransit(id, 1, null));
    }

    @Operation(summary = "已出餐", description = "生成取餐码并进入骑手抢单池")
    @PostMapping("/{id}/ready")
    public Result<Order> ready(@PathVariable String id) {
        return Result.ok(orderService.merchantTransit(id, 2, null));
    }

    @Operation(summary = "拒单", description = "订单取消、回补库存并退款")
    @PostMapping("/{id}/reject")
    public Result<Order> reject(@PathVariable String id, @RequestParam String reason) {
        return Result.ok(orderService.merchantTransit(id, 5, reason));
    }
}
