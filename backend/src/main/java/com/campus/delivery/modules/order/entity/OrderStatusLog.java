package com.campus.delivery.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单状态流转记录。主责：成员4。
 *
 * <p>每次状态变更追加一条，实现订单全程可追溯（需求文档 5.6.2）。
 */
@Data
@TableName("order_status_log")
public class OrderStatusLog implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String orderId;

    private Integer fromStatus;

    private Integer toStatus;

    private String operatorId;

    /** STUDENT / MERCHANT / RIDER / ADMIN / SYSTEM */
    private String operatorRole;

    private String reason;

    private LocalDateTime createTime;
}
