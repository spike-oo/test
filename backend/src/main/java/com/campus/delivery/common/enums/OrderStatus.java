package com.campus.delivery.common.enums;

import com.campus.delivery.common.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 订单状态枚举与状态流转规则。
 *
 * <p>状态取值与数据库 {@code orders.order_status} 一致（见 database/schema.sql）。
 * 真正的流转校验与日志写入在 {@code modules/order/OrderStateMachine} 中完成，
 * 任何模块都不得直接 UPDATE 订单状态字段。
 */
@Getter
public enum OrderStatus {

    PENDING_ACCEPT(0, "待接单"),
    PREPARING(1, "备餐中"),
    PENDING_PICKUP(2, "待取餐"),
    DELIVERING(3, "配送中"),
    FINISHED(4, "已完成"),
    CANCELED(5, "已取消"),
    APPEALING(6, "申诉中");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus of(Integer code) {
        if (code == null) {
            throw new BusinessException(30001, "订单状态为空");
        }
        return Arrays.stream(values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(30002, "非法的订单状态：" + code));
    }

    /**
     * 允许流转到的目标状态集合。
     *
     * <pre>
     *   0 待接单 → 1 备餐中 / 5 已取消
     *   1 备餐中 → 2 待取餐 / 5 已取消
     *   2 待取餐 → 3 配送中 / 5 已取消
     *   3 配送中 → 4 已完成
     *   4 已完成 → 6 申诉中
     *   5 已取消 → 终态
     *   6 申诉中 → 4 已完成 / 5 已取消
     * </pre>
     */
    public Set<OrderStatus> allowedNext() {
        switch (this) {
            case PENDING_ACCEPT:
                return EnumSet.of(PREPARING, CANCELED);
            case PREPARING:
                return EnumSet.of(PENDING_PICKUP, CANCELED);
            case PENDING_PICKUP:
                return EnumSet.of(DELIVERING, CANCELED);
            case DELIVERING:
                return EnumSet.of(FINISHED);
            case FINISHED:
                return EnumSet.of(APPEALING);
            case APPEALING:
                return EnumSet.of(FINISHED, CANCELED);
            case CANCELED:
            default:
                return Collections.emptySet();
        }
    }

    /** 是否允许从当前状态流转到目标状态（状态不可逆跳） */
    public boolean canTransferTo(OrderStatus target) {
        return target != null && allowedNext().contains(target);
    }

    public boolean isFinal() {
        return allowedNext().isEmpty();
    }
}
