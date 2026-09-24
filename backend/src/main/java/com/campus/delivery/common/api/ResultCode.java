package com.campus.delivery.common.api;

import lombok.Getter;

/**
 * 通用响应码。
 *
 * <p>业务细分码段（配合 {@link Result#fail(Integer, String)} 使用）：
 * <ul>
 *   <li>1xxxx 用户与权限</li>
 *   <li>2xxxx 店铺与菜品</li>
 *   <li>3xxxx 购物车与订单</li>
 *   <li>4xxxx 配送与骑手</li>
 *   <li>5xxxx 评价</li>
 *   <li>6xxxx AI 能力</li>
 * </ul>
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    BUSINESS_ERROR(500, "业务处理失败"),
    SYSTEM_ERROR(500, "系统异常，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
