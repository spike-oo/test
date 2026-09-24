package com.campus.delivery.modules.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.enums.OrderStatus;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.service.OrderService;
import com.campus.delivery.modules.review.dto.ReviewSubmitDTO;
import com.campus.delivery.modules.review.dto.ReviewTagDTO;
import com.campus.delivery.modules.review.entity.Review;
import com.campus.delivery.modules.review.mapper.ReviewMapper;
import com.campus.delivery.modules.review.mapper.ReviewTagMapper;
import com.campus.delivery.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 评价服务。主责：成员2。
 *
 * <p>评价提交后由 AI 模块异步做标签提取与情感分析（调用 {@code ReviewAnalysisService}），
 * 分析结果回写 {@code review.ai_tags} / {@code review.sentiment} 并写入 {@code review_tag}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int CODE_ORDER_NOT_FINISHED = 50001;
    private static final int CODE_ALREADY_REVIEWED = 50002;
    private static final int CODE_REVIEW_NOT_FOUND = 50003;

    private final ReviewMapper reviewMapper;
    private final ReviewTagMapper reviewTagMapper;
    private final OrderService orderService;
    private final ShopService shopService;

    /** 提交评价：仅已完成的订单可评价，且一个订单只能评价一次 */
    @Transactional(rollbackFor = Exception.class)
    public Review submit(String userId, ReviewSubmitDTO dto) {
        Order order = orderService.requireOrder(dto.getOrderId());
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(50004, "无权评价他人订单");
        }
        if (!OrderStatus.FINISHED.equals(OrderStatus.of(order.getOrderStatus()))) {
            throw new BusinessException(CODE_ORDER_NOT_FINISHED, "订单尚未完成，暂不能评价");
        }
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(CODE_ALREADY_REVIEWED, "该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setUserId(userId);
        review.setShopId(order.getShopId());
        review.setRiderId(order.getRiderId());
        review.setTasteScore(dto.getTasteScore());
        review.setServiceScore(dto.getServiceScore());
        review.setDeliveryScore(dto.getDeliveryScore());
        review.setContent(dto.getContent());
        review.setIsAnonymous(Boolean.TRUE.equals(dto.getAnonymous()) ? 1 : 0);
        reviewMapper.insert(review);

        // TODO(成员2 + 成员1)：评价图片落库 review_image；调用 AI 做标签提取与情感分析
        //   reviewAnalysisService.analyzeAsync(review.getId());
        log.info("评价提交成功: reviewId={}, orderId={}", review.getId(), order.getId());
        return review;
    }

    /** 店铺评价列表（公开） */
    public PageResult<Review> pageByShop(String shopId, long pageNum, long pageSize) {
        Page<Review> page = reviewMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getShopId, shopId)
                        .orderByDesc(Review::getCreateTime));
        // 匿名评价不返回用户信息
        page.getRecords().forEach(review -> {
            if (Integer.valueOf(1).equals(review.getIsAnonymous())) {
                review.setUserId(null);
            }
        });
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 本店评价列表（商户端） */
    public PageResult<Review> pageMyShopReviews(Integer sentiment, long pageNum, long pageSize) {
        Page<Review> page = reviewMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getShopId, shopService.currentShopId())
                        .eq(sentiment != null, Review::getSentiment, sentiment)
                        .orderByDesc(Review::getCreateTime));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 商家回复评价 */
    @Transactional(rollbackFor = Exception.class)
    public void reply(String reviewId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(50005, "回复内容不能为空");
        }
        Review review = requireOwnReview(reviewId);
        review.setMerchantReply(content);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    /** 标签聚合（供商户端标签云、好评点/吐槽点排行使用） */
    public List<Map<String, Object>> statTags(int limit) {
        return reviewMapper.statTags(shopService.currentShopId(), limit);
    }

    /** 评价概览（评分、好评率） */
    public Map<String, Object> statOverview() {
        return reviewMapper.statOverview(shopService.currentShopId());
    }

    /**
     * 回写 AI 分析结果（由 AI 模块调用，保证评价表只有本服务写入）。
     *
     * @param tagsCsv      标签汇总，逗号分隔，写入 review.ai_tags
     * @param sentiment    情感倾向：0差评 1中评 2好评
     * @param modelVersion 模型版本，保证 AI 结果可追溯
     * @param tags         标签明细，写入 review_tag 供聚合统计
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAiAnalysis(String reviewId, String tagsCsv, Integer sentiment,
                               String modelVersion, List<ReviewTagDTO> tags) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(CODE_REVIEW_NOT_FOUND, "评价不存在");
        }
        review.setAiTags(tagsCsv);
        review.setSentiment(sentiment);
        review.setAiModelVersion(modelVersion);
        review.setAiAnalyzeTime(LocalDateTime.now());
        reviewMapper.updateById(review);

        if (tags != null) {
            for (ReviewTagDTO tag : tags) {
                reviewTagMapper.insert(UUID.randomUUID().toString().replace("-", ""),
                        reviewId, review.getShopId(), tag.getTagName(), tag.getTagType(), tag.getSentiment());
            }
        }
    }

    private Review requireOwnReview(String reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(CODE_REVIEW_NOT_FOUND, "评价不存在");
        }
        if (!shopService.currentShopId().equals(review.getShopId())) {
            throw new BusinessException(50006, "无权回复其他店铺的评价");
        }
        return review;
    }
}
