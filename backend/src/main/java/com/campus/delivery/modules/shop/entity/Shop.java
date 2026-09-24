package com.campus.delivery.modules.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺表。主责：成员2。
 */
@Data
@TableName("shop")
public class Shop implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属商户ID：商户端数据行级隔离的依据 */
    private String merchantId;

    private String categoryId;

    private String shopName;

    private String logo;

    private String description;

    private String address;

    private String phone;

    private String notice;

    private String openTime;

    /** 0休息中 1营业中 */
    private Integer businessStatus;

    /** 0正常 1已封禁 */
    private Integer banStatus;

    private BigDecimal minPrice;

    private BigDecimal deliveryFee;

    private Integer deliveryTime;

    private BigDecimal score;

    private Integer monthlySales;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
