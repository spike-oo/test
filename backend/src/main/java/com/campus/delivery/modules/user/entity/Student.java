package com.campus.delivery.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生用户表。主责：成员1。
 */
@Data
@TableName("student")
public class Student implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String studentNo;

    private String realName;

    private String college;

    private String grade;

    /** 账户余额（模拟支付用），禁止出现负数 */
    private java.math.BigDecimal balance;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
