package com.campus.delivery.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求。四种角色共用同一入口，{@code role} 可选，用于校验登录入口是否与账号角色一致。
 */
@Data
public class LoginRequest implements Serializable {

    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 可选：STUDENT / MERCHANT / RIDER / ADMIN */
    private String role;
}
