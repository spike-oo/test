package com.campus.delivery.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户饮食档案：AI 个性化推荐、忌口过滤、饮食分析的输入数据。
 *
 * <p>主责：成员1（对应需求文档 5.6.5 难点五：用户画像与推荐冷启动）。
 */
@Data
@TableName("diet_profile")
public class DietProfile implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    /** 0普通 1减脂 2增肌 */
    private Integer dietGoal;

    /** 口味偏好，逗号分隔，如 "清淡,微辣" */
    private String tastePreference;

    /** 忌口/过敏原食材，AI 点餐时据此过滤菜品 */
    private String allergyFoods;

    private String dislikeFoods;

    private Integer heightCm;

    private BigDecimal weightKg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
