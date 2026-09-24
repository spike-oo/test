package com.campus.delivery.modules.delivery.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 抢单日志数据访问。主责：成员4。
 *
 * <p>抢单日志只做追加写入与统计，无需实体映射，直接用注解 SQL。
 */
@Mapper
public interface GrabLogMapper {

    /** 记录一次抢单尝试（成功或失败），作为并发抢单的审计依据 */
    @Insert("INSERT INTO grab_log (id, order_id, rider_id, result, fail_reason, create_time) "
            + "VALUES (#{id}, #{orderId}, #{riderId}, #{result}, #{failReason}, NOW())")
    int insert(@Param("id") String id,
               @Param("orderId") String orderId,
               @Param("riderId") String riderId,
               @Param("result") int result,
               @Param("failReason") String failReason);

    /** 骑手收入概览：今日 / 本周 / 本月 */
    @Select("SELECT "
            + "IFNULL(SUM(CASE WHEN DATE(finish_time) = CURDATE() THEN delivery_fee END), 0) AS today_income, "
            + "IFNULL(SUM(CASE WHEN YEARWEEK(finish_time, 1) = YEARWEEK(CURDATE(), 1) THEN delivery_fee END), 0) AS week_income, "
            + "IFNULL(SUM(CASE WHEN DATE_FORMAT(finish_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') THEN delivery_fee END), 0) AS month_income, "
            + "COUNT(CASE WHEN DATE(finish_time) = CURDATE() THEN 1 END) AS today_count, "
            + "COUNT(CASE WHEN DATE_FORMAT(finish_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') THEN 1 END) AS month_count "
            + "FROM delivery_record WHERE rider_id = #{riderId} AND status = 2")
    Map<String, Object> statIncome(@Param("riderId") String riderId);

    /** 单笔配送费合计（用于累计收入核对） */
    @Select("SELECT IFNULL(SUM(delivery_fee), 0) FROM delivery_record WHERE rider_id = #{riderId} AND status = 2")
    BigDecimal sumIncome(@Param("riderId") String riderId);
}
