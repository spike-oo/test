package com.campus.delivery.modules.review.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评价标签数据访问。主责：成员2，写入方：AI 评价分析。
 *
 * <p>标签只做追加写入与聚合统计，无需实体映射，直接用注解 SQL。
 */
@Mapper
public interface ReviewTagMapper {

    /** 追加一条 AI 提取的评价标签 */
    @Insert("INSERT INTO review_tag (id, review_id, shop_id, tag_name, tag_type, sentiment, create_time) "
            + "VALUES (#{id}, #{reviewId}, #{shopId}, #{tagName}, #{tagType}, #{sentiment}, NOW())")
    int insert(@Param("id") String id,
               @Param("reviewId") String reviewId,
               @Param("shopId") String shopId,
               @Param("tagName") String tagName,
               @Param("tagType") String tagType,
               @Param("sentiment") Integer sentiment);
}
