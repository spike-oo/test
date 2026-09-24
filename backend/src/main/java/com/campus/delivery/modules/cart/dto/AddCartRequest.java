package com.campus.delivery.modules.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 加入购物车入参。
 */
@Data
public class AddCartRequest implements Serializable {

    private String dishId;

    private String specId;

    private Integer quantity;
}
