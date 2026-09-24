package com.campus.delivery.modules.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人中心展示对象：账号信息 + 学生档案 + 饮食档案（手机号已脱敏）。
 */
@Data
public class UserProfileVO implements Serializable {

    private String userId;

    private String username;

    private String nickname;

    /** 已脱敏 */
    private String phone;

    private String avatar;

    private String role;

    private String studentNo;

    private String college;

    private String grade;

    /** 0普通 1减脂 2增肌 */
    private Integer dietGoal;

    private String tastePreference;

    private String allergyFoods;
}
