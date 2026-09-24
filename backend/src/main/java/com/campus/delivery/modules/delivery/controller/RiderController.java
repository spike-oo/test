package com.campus.delivery.modules.delivery.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.delivery.entity.Rider;
import com.campus.delivery.modules.delivery.service.DeliveryService;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 骑手端接口（P2 可选加分）。主责：成员4。
 *
 * <p>核心功能稳定后再开发；未开发时不影响主线编译与运行。
 */
@Tag(name = "10-骑手端（P2）", description = "订单大厅、抢单、取餐、送达、收入统计")
@RestController
@RequestMapping("/rider")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.RIDER)
public class RiderController {

    private final DeliveryService deliveryService;

    @Operation(summary = "订单大厅", description = "可抢的待配送订单列表")
    @GetMapping("/hall")
    public Result<PageResult<Order>> hall(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(deliveryService.orderHall(pageNum, pageSize));
    }

    @Operation(summary = "抢单", description = "Redis 分布式锁 + 数据库条件更新，保证同一订单只有一个骑手抢到")
    @PostMapping("/grab/{orderId}")
    public Result<Order> grab(@PathVariable String orderId) {
        return Result.ok(deliveryService.grab(currentRiderId(), orderId));
    }

    @Operation(summary = "确认取餐", description = "订单状态变更为配送中")
    @PostMapping("/pickup/{orderId}")
    public Result<Order> pickup(@PathVariable String orderId) {
        return Result.ok(deliveryService.pickup(currentRiderId(), orderId));
    }

    @Operation(summary = "确认送达", description = "订单完成，收入累加")
    @PostMapping("/deliver/{orderId}")
    public Result<Order> deliver(@PathVariable String orderId) {
        return Result.ok(deliveryService.deliver(currentRiderId(), orderId));
    }

    @Operation(summary = "我的配送订单")
    @GetMapping("/orders")
    public Result<PageResult<Order>> myOrders(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(deliveryService.myDeliveringOrders(currentRiderId(), status, pageNum, pageSize));
    }

    @Operation(summary = "收入统计", description = "今日/本周/本月收入与订单量")
    @GetMapping("/income")
    public Result<Map<String, Object>> income() {
        return Result.ok(deliveryService.income(currentRiderId()));
    }

    @Operation(summary = "切换工作状态", description = "0休息中 1接单中")
    @PutMapping("/work-status")
    public Result<Void> updateWorkStatus(@RequestParam Integer workStatus) {
        deliveryService.updateWorkStatus(currentRiderId(), workStatus);
        return Result.ok();
    }

    @Operation(summary = "我的骑手档案")
    @GetMapping("/profile")
    public Result<Rider> profile() {
        return Result.ok(deliveryService.getByUserId(UserContext.getUserId()));
    }

    /** 当前登录账号对应的骑手ID */
    private String currentRiderId() {
        Rider rider = deliveryService.getByUserId(UserContext.getUserId());
        if (rider == null) {
            throw new com.campus.delivery.common.exception.BusinessException(40001, "当前账号未绑定骑手信息");
        }
        return rider.getId();
    }
}
