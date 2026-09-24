package com.campus.delivery.security;

import com.campus.delivery.common.api.ResultCode;
import com.campus.delivery.common.exception.BusinessException;

/**
 * 当前登录人上下文。
 *
 * <p>由 {@link AuthInterceptor} 在请求进入时写入、请求结束时清除。
 * 业务代码通过 {@link #getUserId()} / {@link #getRole()} 获取身份，
 * 据此拼接数据行级过滤条件（如商户只能查自己店铺的订单）。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 获取当前登录人，未登录直接抛 401 */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static String getUserId() {
        return require().getUserId();
    }

    public static String getRole() {
        return require().getRole();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && user.isAdmin();
    }
}
