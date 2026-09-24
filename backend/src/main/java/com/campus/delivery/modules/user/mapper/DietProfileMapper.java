package com.campus.delivery.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.user.entity.DietProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饮食档案数据访问。主责：成员1。
 */
@Mapper
public interface DietProfileMapper extends BaseMapper<DietProfile> {
}
