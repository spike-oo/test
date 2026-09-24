package com.campus.delivery.modules.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食档案维护入参（对应需求文档 5.1.7 个人中心与饮食分析）。
 */
@Data
public class DietProfileDTO implements Serializable {

    /** 0普通 1减脂 2增肌 */
    private Integer dietGoal;

    /** 口味偏好，逗号分隔 */
    private String tastePreference;

    /** 忌口/过敏原食材，逗号分隔；AI 点餐时据此过滤菜品 */
    private String allergyFoods;

    private String dislikeFoods;

    private Integer heightCm;

    private java.math.BigDecimal weightKg;
}
