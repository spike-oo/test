package com.campus.delivery.modules.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车展示对象：购物车记录 + 菜品快照信息 + 小计金额。
 *
 * <p>金额由后端计算并返回，前端只做展示，避免前后端金额口径不一致。
 */
@Data
public class CartItemVO implements Serializable {

    private String id;

    private String shopId;

    private String shopName;

    private String dishId;

    private String dishName;

    private String dishImage;

    private String specId;

    private String specName;

    private BigDecimal price;

    private Integer quantity;

    private Integer selected;

    /** 小计金额 = price * quantity */
    private BigDecimal amount;

    /** 当前库存，前端据此提示库存不足 */
    private Integer stock;

    /** 菜品是否仍在上架：0下架 1上架 */
    private Integer dishStatus;
}
