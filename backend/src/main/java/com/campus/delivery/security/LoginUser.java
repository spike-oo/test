package com.campus.delivery.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录态载体：由 JWT 解析得到，保存在 {@link UserContext} 的 ThreadLocal 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    /** 用户ID（sys_user.id） */
    private String userId;
    /** 登录账号 */
    private String username;
    /** 角色编码：STUDENT / MERCHANT / RIDER / ADMIN */
    private String role;
    /** 昵称 */
    private String nickname;

    public boolean isStudent() {
        return RoleEnum.STUDENT.getCode().equals(role);
    }

    public boolean isMerchant() {
        return RoleEnum.MERCHANT.getCode().equals(role);
    }

    public boolean isRider() {
        return RoleEnum.RIDER.getCode().equals(role);
    }

    public boolean isAdmin() {
        return RoleEnum.ADMIN.getCode().equals(role);
    }
}
