package com.campus.delivery.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生注册请求。注册后自动创建账号、学生档案与空白饮食档案。
 */
@Data
public class RegisterRequest implements Serializable {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20位")
    private String studentNo;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String college;

    private String grade;
}
