package com.campus.delivery.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 收货地址新增/修改入参。
 */
@Data
public class AddressDTO implements Serializable {

    /** 修改时必填 */
    private String id;

    @NotBlank(message = "收货人不能为空")
    private String receiver;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String campusArea;

    private String building;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否设为默认地址 */
    private Boolean isDefault;
}
