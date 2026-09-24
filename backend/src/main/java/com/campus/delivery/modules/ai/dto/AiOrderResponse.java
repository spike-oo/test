package com.campus.delivery.modules.ai.dto;

import com.campus.delivery.modules.dish.entity.Dish;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI 点餐 / 语义搜索响应。
 *
 * <p>{@code fallback = true} 表示模型调用或 JSON 解析失败，已降级为关键词检索，
 * 前端应展示「AI 理解失败，已为你按关键词推荐」之类的提示。
 */
@Data
public class AiOrderResponse implements Serializable {

    /** AI 的自然语言回复（含推荐理由） */
    private String reply;

    /** 解析出的结构化需求条件 */
    private Map<String, Object> conditions;

    /** 匹配到的菜品 */
    private List<Dish> dishes;

    /** 是否走了降级逻辑 */
    private Boolean fallback;

    /** 会话ID（多轮对话时前端回传） */
    private String sessionId;

    /** 模型版本，便于问题排查与结果追溯 */
    private String modelVersion;
}
