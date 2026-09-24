package com.campus.delivery.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.user.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生用户数据访问。主责：成员1。
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
