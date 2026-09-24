package com.campus.delivery.modules.order.service;

import com.campus.delivery.common.enums.OrderStatus;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.entity.OrderStatusLog;
import com.campus.delivery.modules.order.mapper.OrderMapper;
import com.campus.delivery.modules.order.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单状态机。主责：成员4。
 *
 * <p><b>全系统唯一允许修改 {@code orders.order_status} 的入口</b>，
 * 各端（学生取消、商户接单出餐、骑手取餐送达）都必须调用本类。
 *
 * <p>保证：
 * <ol>
 *   <li>状态不可逆跳，非法流转直接抛业务异常；</li>
 *   <li>每次流转追加一条 {@code order_status_log}，全程可追溯；</li>
 *   <li>配合订单表 {@code version} 乐观锁，防止并发覆盖。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStateMachine {

    private static final int CODE_ILLEGAL_TRANSITION = 30001;
    private static final int CODE_CONCURRENT_MODIFIED = 30002;

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    /**
     * 执行状态流转。
     *
     * @param order          订单实体（须为最新数据）
     * @param target         目标状态
     * @param operatorId     操作人ID
     * @param operatorRole   操作人角色：STUDENT / MERCHANT / RIDER / ADMIN / SYSTEM
     * @param reason         变更原因（拒单、取消、申诉等场景必填）
     * @return 流转后的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order transit(Order order, OrderStatus target, String operatorId,
                         String operatorRole, String reason) {
        OrderStatus current = OrderStatus.of(order.getOrderStatus());
        if (!current.canTransferTo(target)) {
            throw new BusinessException(CODE_ILLEGAL_TRANSITION,
                    "订单当前为「" + current.getDesc() + "」，不允许变更为「" + target.getDesc() + "」");
        }

        order.setOrderStatus(target.getCode());
        if (target == OrderStatus.FINISHED) {
            order.setFinishTime(java.time.LocalDateTime.now());
        }
        if (target == OrderStatus.CANCELED) {
            order.setCancelReason(reason);
        }

        // 乐观锁：version 不匹配时更新行数为 0
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(CODE_CONCURRENT_MODIFIED, "订单状态已被其他操作变更，请刷新后重试");
        }

        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(order.getId());
        statusLog.setFromStatus(current.getCode());
        statusLog.setToStatus(target.getCode());
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorRole(operatorRole);
        statusLog.setReason(reason);
        orderStatusLogMapper.insert(statusLog);

        log.info("订单状态流转: orderId={}, {} -> {}, operator={}({})",
                order.getId(), current.getDesc(), target.getDesc(), operatorId, operatorRole);
        return order;
    }
}
