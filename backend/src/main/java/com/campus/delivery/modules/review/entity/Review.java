package com.campus.delivery.modules.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价表。主责：成员2。
 *
 * <p>{@code aiTags} 与 {@code sentiment} 由 AI 模块（成员1/成员2）在评价提交后异步写入，
 * 并记录 {@code aiModelVersion} 与 {@code aiAnalyzeTime}，保证 AI 结果可追溯。
 */
@Data
@TableName("review")
public class Review implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String orderId;

    private String userId;

    private String shopId;

    private String riderId;

    /** 口味评分 1-5 */
    private Integer tasteScore;

    /** 商家服务评分 1-5 */
    private Integer serviceScore;

    /** 配送评分 1-5 */
    private Integer deliveryScore;

    private String content;

    /** 0否 1是 */
    private Integer isAnonymous;

    /** AI 提取的标签，逗号分隔 */
    private String aiTags;

    /** 0差评 1中评 2好评 */
    private Integer sentiment;

    private String aiModelVersion;

    private LocalDateTime aiAnalyzeTime;

    private String merchantReply;

    private LocalDateTime replyTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
