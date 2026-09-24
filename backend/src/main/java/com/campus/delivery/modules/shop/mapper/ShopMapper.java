package com.campus.delivery.modules.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

/**
 * 店铺数据访问。主责：成员2。
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
}
