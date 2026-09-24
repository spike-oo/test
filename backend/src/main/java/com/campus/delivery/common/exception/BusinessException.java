package com.campus.delivery.common.exception;

import com.campus.delivery.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由业务规则主动抛出，交给 GlobalExceptionHandler 统一转换为响应体。
 *
 * <p>使用示例：
 * <pre>
 *   if (dish.getStock() &lt; quantity) {
 *       throw new BusinessException("库存不足，请调整数量");
 *   }
 *   throw new BusinessException(30001, "订单状态已变更，请刷新后重试");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
