package com.campus.delivery.modules.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户表。主责：成员2（商品与经营域）。
 *
 * <p>说明：商户是「商品与经营域」的主体，因此放在 shop 模块；
 * 账号登录信息在 {@code sys_user} 中，通过 user_id 关联。
 */
@Data
@TableName("merchant")
public class Merchant implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String merchantName;

    private String contactName;

    private String contactPhone;

    private String licenseNo;

    /** 0待审核 1已通过 2已驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime auditTime;

    private String auditBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
