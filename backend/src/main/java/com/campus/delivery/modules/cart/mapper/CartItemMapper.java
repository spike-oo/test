package com.campus.delivery.modules.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车数据访问。主责：成员1。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
