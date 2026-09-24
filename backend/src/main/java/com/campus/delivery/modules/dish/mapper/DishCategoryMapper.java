package com.campus.delivery.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.dish.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品分类数据访问。主责：成员2。
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
