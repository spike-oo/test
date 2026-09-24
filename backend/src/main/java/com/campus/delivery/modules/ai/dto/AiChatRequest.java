package com.campus.delivery.modules.ai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 对话入参（AI 点餐、语义搜索、智能客服共用）。
 */
@Data
public class AiChatRequest implements Serializable {

    /** 会话ID：智能客服多轮对话使用；为空时后端新建会话 */
    private String sessionId;

    /** 用户自然语言输入 */
    private String message;

    /** 限定店铺（可选，用于店铺内 AI 点餐） */
    private String shopId;

    /** 返回条数上限 */
    private Integer limit;
}
