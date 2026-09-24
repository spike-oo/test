package com.campus.delivery.modules.delivery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 骑手表。主责：成员4（P2 可选加分功能）。
 *
 * <p>不接入真实骑手硬件与定位，仅做校园内简化的派单/抢单与状态流转。
 */
@Data
@TableName("rider")
public class Rider implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String realName;

    /** 身份证号：加密存储，展示脱敏 */
    private String idCard;

    private String studentCardNo;

    private String vehicleType;

    /** 0休息中 1接单中 */
    private Integer workStatus;

    /** 当前配送中订单数，抢单成功 +1，完成 -1 */
    private Integer deliveringCount;

    /** 同时最大接单数 */
    private Integer maxDelivering;

    private BigDecimal totalIncome;

    private Integer avgDeliveryTime;

    /** 0待审核 1已通过 2已驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
