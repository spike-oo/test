package com.campus.delivery.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址数据访问。主责：成员1。
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
