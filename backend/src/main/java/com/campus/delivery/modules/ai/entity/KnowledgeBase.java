package com.campus.delivery.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服知识库。主责：成员3（维护在管理端），使用方：AI 智能客服。
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 分类：订单 / 退款 / 配送 / 账号 / AI 点餐 */
    private String category;

    private String question;

    private String answer;

    /** 关键词，逗号分隔，用于检索召回 */
    private String keywords;

    private Integer hitCount;

    /** 0停用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
