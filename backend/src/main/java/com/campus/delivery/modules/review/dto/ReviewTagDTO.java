package com.campus.delivery.modules.review.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 提取出的单条评价标签。
 */
@Data
public class ReviewTagDTO implements Serializable {

    /** 标签名，如「口味好」「出餐慢」 */
    private String tagName;

    /** 维度：TASTE口味 / PORTION分量 / SPEED速度 / HYGIENE卫生 / SERVICE服务 / PRICE价格 */
    private String tagType;

    /** 情感：0负面 1中性 2正面 */
    private Integer sentiment;
}
