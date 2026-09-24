package com.campus.delivery.security;

import lombok.Getter;

/**
 * 系统角色。与数据库 {@code sys_user.role} 取值一致。
 *
 * <p>数据范围约定（见 docs/02-四人分工与模块归属.md）：
 * <ul>
 *   <li>STUDENT：只能访问自己的数据（订单、购物车、评价、画像）</li>
 *   <li>MERCHANT：只能访问自己店铺的数据（菜品、订单、评价）</li>
 *   <li>RIDER：只能访问自己的配送订单</li>
 *   <li>ADMIN：全局数据</li>
 * </ul>
 */
@Getter
public enum RoleEnum {

    STUDENT("STUDENT", "学生"),
    MERCHANT("MERCHANT", "商户"),
    RIDER("RIDER", "骑手"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum of(String code) {
        for (RoleEnum role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return null;
    }
}
