package com.campus.delivery.modules.review.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交评价入参（需求文档 5.1.6 评价系统）。
 */
@Data
public class ReviewSubmitDTO implements Serializable {

    private String orderId;

    /** 口味评分 1-5 */
    private Integer tasteScore;

    /** 商家服务评分 1-5 */
    private Integer serviceScore;

    /** 配送评分 1-5 */
    private Integer deliveryScore;

    private String content;

    private Boolean anonymous;

    /** 评价图片URL列表（已上传到对象存储） */
    private List<String> images;
}
