package com.campus.delivery.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表。主责：成员4。
 *
 * <p>{@code orderStatus} 只能通过 {@code OrderStateMachine} 变更，
 * {@code version} 为乐观锁版本号，防止并发下的非法状态写入。
 */
@Data
@TableName("orders")
public class Order implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 对外展示的订单编号 */
    private String orderNo;

    private String userId;

    private String shopId;

    private String riderId;

    private BigDecimal goodsAmount;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    /** 1余额 2模拟第三方 */
    private Integer payType;

    /** 0未支付 1已支付 2已退款 */
    private Integer payStatus;

    private LocalDateTime payTime;

    /** 0待接单 1备餐中 2待取餐 3配送中 4已完成 5已取消 6申诉中 */
    private Integer orderStatus;

    /** 1骑手配送 2宿舍楼下自取 */
    private Integer deliveryType;

    private String receiver;

    private String receiverPhone;

    private String address;

    private String remark;

    private String pickupCode;

    private String cancelReason;

    private LocalDateTime expectTime;

    private LocalDateTime finishTime;

    @Version
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
