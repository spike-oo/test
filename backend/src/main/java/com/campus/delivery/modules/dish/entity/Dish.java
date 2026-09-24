package com.campus.delivery.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品表。主责：成员2。
 *
 * <p>{@code tags} 与 {@code ingredients} 是 AI 语义搜索、忌口/过敏原过滤的关键字段。
 */
@Data
@TableName("dish")
public class Dish implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String shopId;

    private String categoryId;

    private String dishName;

    private BigDecimal price;

    private String image;

    private String description;

    /** 菜品标签，逗号分隔，如 "清淡,低脂,不辣" */
    private String tags;

    /** 主要食材，逗号分隔，过敏原过滤依据 */
    private String ingredients;

    /** 库存，不可为负 */
    private Integer stock;

    /** 0下架 1上架 */
    private Integer status;

    private Integer monthlySales;

    private BigDecimal score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
