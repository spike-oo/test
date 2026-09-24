package com.campus.delivery.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服工单表。主责：成员3（AI 客服无法回答时转人工）。
 */
@Data
@TableName("ticket")
public class Ticket implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String ticketNo;

    private String userId;

    private String sessionId;

    private String orderId;

    /** 问题分类（AI 自动分类） */
    private String category;

    private String question;

    /** 0待处理 1处理中 2已关闭 */
    private Integer status;

    private String handlerId;

    private String reply;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime closeTime;
}
