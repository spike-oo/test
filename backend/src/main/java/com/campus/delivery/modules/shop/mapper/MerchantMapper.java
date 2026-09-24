package com.campus.delivery.modules.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.shop.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户数据访问。主责：成员2。
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
