package com.campus.delivery.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体：{@code { "code": 200, "message": "success", "data": {...} }}
 *
 * <p>所有 Controller 一律返回本类型，禁止直接返回 Entity。
 * 约定见 docs/03-接口规范与协作约定.md。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.BUSINESS_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 业务码段：1xxxx 用户权限 / 2xxxx 店铺菜品 / 3xxxx 订单 / 4xxxx 配送 / 5xxxx 评价 / 6xxxx AI */
    public static <T> Result<T> fail(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
