package com.campus.delivery.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解：标注在 Controller 类或方法上，由 {@link AuthInterceptor} 校验。
 *
 * <p>使用示例：
 * <pre>
 *   &#64;RequiresRole(RoleEnum.STUDENT)          // 仅学生可访问
 *   &#64;RequiresRole({RoleEnum.MERCHANT, RoleEnum.ADMIN})  // 商户或管理员可访问
 * </pre>
 *
 * <p>不标注该注解时，仅要求已登录（公开接口请加入
 * {@code WebMvcConfig} 的放行路径白名单）。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /** 允许访问的角色，满足任一即可 */
    RoleEnum[] value();
}
