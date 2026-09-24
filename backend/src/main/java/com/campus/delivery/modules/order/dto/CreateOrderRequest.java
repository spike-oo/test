package com.campus.delivery.modules.order.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交订单入参（购物车结算）。
 */
@Data
public class CreateOrderRequest implements Serializable {

    /** 收货地址ID；宿舍楼下自取时可空 */
    private String addressId;

    /** 1骑手配送 2宿舍楼下自取 */
    private Integer deliveryType;

    /** 1余额 2模拟第三方 */
    private Integer payType;

    /** 备注（忌口等） */
    private String remark;

    /** 收货人姓名（自取时可空） */
    private String receiver;

    /** 收货电话（自取时可空） */
    private String receiverPhone;

    /** 收货地址文本（自取时可空） */
    private String address;
}
