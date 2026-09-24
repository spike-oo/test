package com.campus.delivery.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表。主责：成员4。
 *
 * <p>菜品名称、价格在下单时快照保存，保证菜品改名或调价后历史订单不受影响。
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String orderId;

    private String dishId;

    private String dishName;

    private String dishImage;

    private String specName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal amount;

    private LocalDateTime createTime;
}
