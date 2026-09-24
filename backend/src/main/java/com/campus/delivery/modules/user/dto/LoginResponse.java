package com.campus.delivery.modules.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应。前端把 token 存入 Pinia 与 localStorage，后续请求放在 Authorization 头。
 */
@Data
public class LoginResponse implements Serializable {

    private String token;

    private String userId;

    private String username;

    private String nickname;

    private String role;

    private String avatar;

    /** Token 有效期（秒） */
    private Long expiresIn;
}
