package com.campus.delivery.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细数据访问。主责：成员4。
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
