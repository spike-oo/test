package com.campus.delivery.modules.order.dto;

import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 订单详情展示对象：订单主体 + 明细 + 状态中文描述。
 */
@Data
public class OrderVO implements Serializable {

    private Order order;

    private List<OrderItem> items;

    /** 状态中文描述，如「备餐中」 */
    private String statusDesc;

    /** 商户名称 / 店铺名称，便于列表直接展示 */
    private String shopName;
}
