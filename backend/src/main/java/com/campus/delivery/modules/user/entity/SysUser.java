package com.campus.delivery.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号表（四种角色共用的登录底座）。
 *
 * <p>主责：成员4（认证） / 成员1（学生资料）。
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 登录账号：学生用学号，商户/骑手用手机号，管理员自定义 */
    private String username;

    /** BCrypt 加盐哈希，禁止明文 */
    private String password;

    /** STUDENT / MERCHANT / RIDER / ADMIN */
    private String role;

    private String nickname;

    /** 手机号：返回前端前必须脱敏 */
    private String phone;

    private String avatar;

    /** 0禁用 1正常 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
