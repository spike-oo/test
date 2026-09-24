package com.campus.delivery.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号数据访问。主责：成员4。
 *
 * <p>单表操作继承 {@link BaseMapper} 即可；复杂统计 SQL 写在
 * {@code resources/mapper/UserMapper.xml} 中。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
