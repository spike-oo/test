package com.campus.delivery.modules.order.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.order.dto.CreateOrderRequest;
import com.campus.delivery.modules.order.dto.OrderVO;
import com.campus.delivery.modules.order.service.OrderService;
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
 * 学生端订单接口。主责：成员4。
 */
@Tag(name = "08-订单（学生端）", description = "下单、订单列表、详情、取消")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "提交订单", description = "购物车结算 + 模拟支付")
    @PostMapping
    @RequiresRole(RoleEnum.STUDENT)
    public Result<OrderVO> create(@RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.createOrder(UserContext.getUserId(), request));
    }

    @Operation(summary = "我的订单列表", description = "可按状态筛选")
    @GetMapping
    public Result<PageResult<OrderVO>> myOrders(@RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(orderService.pageMyOrders(UserContext.getUserId(), status, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable String id) {
        return Result.ok(orderService.getDetail(id, UserContext.getUserId(), UserContext.getRole()));
    }

    @Operation(summary = "取消订单", description = "仅待接单状态可取消，取消后回补库存")
    @PostMapping("/{id}/cancel")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<Void> cancel(@PathVariable String id, @RequestParam(required = false) String reason) {
        orderService.cancelByStudent(UserContext.getUserId(), id, reason);
        return Result.ok();
    }
}
