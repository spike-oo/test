package com.campus.delivery.modules.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购物车表。主责：成员1。
 *
 * <p>同一学生的购物车按店铺分组，只有同一店铺的菜品才能一起结算。
 */
@Data
@TableName("cart_item")
public class CartItem implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String shopId;

    private String dishId;

    private String specId;

    private Integer quantity;

    /** 0未勾选 1已勾选结算 */
    private Integer selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
