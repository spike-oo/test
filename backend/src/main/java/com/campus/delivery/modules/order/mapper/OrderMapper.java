package com.campus.delivery.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单数据访问。主责：成员4。
 *
 * <p>单表操作继承 {@link BaseMapper}；数据看板所需的统计查询写在本接口
 * （复杂 SQL 建议移到 {@code resources/mapper/OrderMapper.xml}）。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 数据看板：按日统计订单量与销售额（供成员3调用）。
     *
     * <p>返回字段：stat_date / order_count / sales_amount
     */
    @Select("SELECT DATE(create_time) AS stat_date, COUNT(*) AS order_count, "
            + "IFNULL(SUM(total_amount), 0) AS sales_amount "
            + "FROM orders WHERE deleted = 0 AND order_status = 4 "
            + "AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) "
            + "GROUP BY DATE(create_time) ORDER BY stat_date")
    List<Map<String, Object>> statDailyTrend(@Param("days") int days);
}
