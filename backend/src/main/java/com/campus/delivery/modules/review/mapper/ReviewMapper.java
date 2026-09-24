package com.campus.delivery.modules.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 评价数据访问。主责：成员2。
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 店铺评价标签聚合：标签名 + 出现次数 + 情感倾向。
     *
     * <p>用于商户端标签云、好评点排行、吐槽点排行（需求文档 5.2.4）。
     */
    @Select("SELECT tag_name AS tagName, sentiment AS sentiment, COUNT(*) AS tagCount "
            + "FROM review_tag WHERE shop_id = #{shopId} "
            + "GROUP BY tag_name, sentiment ORDER BY tagCount DESC LIMIT #{limit}")
    List<Map<String, Object>> statTags(@Param("shopId") String shopId, @Param("limit") int limit);

    /** 店铺评分概览：评价总数、平均分、好评率 */
    @Select("SELECT COUNT(*) AS reviewCount, "
            + "IFNULL(ROUND(AVG(taste_score), 2), 0) AS avgTasteScore, "
            + "IFNULL(ROUND(AVG(service_score), 2), 0) AS avgServiceScore, "
            + "IFNULL(ROUND(AVG(delivery_score), 2), 0) AS avgDeliveryScore, "
            + "IFNULL(SUM(CASE WHEN sentiment = 2 THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0) * 100, 0) AS goodRate "
            + "FROM review WHERE shop_id = #{shopId}")
    Map<String, Object> statOverview(@Param("shopId") String shopId);
}
