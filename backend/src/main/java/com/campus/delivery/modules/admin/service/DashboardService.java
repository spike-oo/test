package com.campus.delivery.modules.admin.service;

import com.campus.delivery.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端数据看板。主责：成员3（需求文档 5.4.4）。
 *
 * <p>取数说明：平台级统计需要跨多个业务域汇总，这里使用**只读**聚合查询
 * （{@link JdbcTemplate}）一次性取数，不修改任何业务数据；
 * 单个业务域的明细数据仍通过对应模块的 Service 获取（如订单趋势走 {@link OrderService}）。
 *
 * <p>性能约定：看板接口全部走缓存或聚合 SQL，避免逐条查询（需求文档 6.1：图表加载 ≤ 3s）。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;
    private final OrderService orderService;

    /** 核心指标 */
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderCount", queryLong("SELECT COUNT(*) FROM orders WHERE deleted = 0"));
        result.put("salesAmount", queryDecimal("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE deleted = 0 AND order_status = 4"));
        result.put("studentCount", queryLong("SELECT COUNT(*) FROM student"));
        result.put("merchantCount", queryLong("SELECT COUNT(*) FROM merchant WHERE audit_status = 1"));
        result.put("shopCount", queryLong("SELECT COUNT(*) FROM shop WHERE deleted = 0"));
        result.put("riderCount", queryLong("SELECT COUNT(*) FROM rider WHERE audit_status = 1"));
        result.put("reviewCount", queryLong("SELECT COUNT(*) FROM review"));
        result.put("ticketWaiting", queryLong("SELECT COUNT(*) FROM ticket WHERE status = 0"));
        result.put("pendingMerchantAudit", queryLong("SELECT COUNT(*) FROM merchant WHERE audit_status = 0"));
        result.put("pendingRiderAudit", queryLong("SELECT COUNT(*) FROM rider WHERE audit_status = 0"));
        return result;
    }

    /** 订单量与销售额趋势（ECharts 折线图数据源） */
    public List<Map<String, Object>> orderTrend(Integer days) {
        return orderService.dailyTrend(days == null ? 7 : days);
    }

    /** 店铺销量排行（柱状图数据源） */
    public List<Map<String, Object>> shopRanking(int limit) {
        return jdbcTemplate.queryForList(
                "SELECT shop_name AS shopName, monthly_sales AS monthlySales, score "
                        + "FROM shop WHERE deleted = 0 ORDER BY monthly_sales DESC LIMIT ?", limit);
    }

    /** 菜品销量排行 */
    public List<Map<String, Object>> dishRanking(int limit) {
        return jdbcTemplate.queryForList(
                "SELECT d.dish_name AS dishName, d.monthly_sales AS monthlySales, s.shop_name AS shopName "
                        + "FROM dish d JOIN shop s ON d.shop_id = s.id "
                        + "WHERE d.deleted = 0 ORDER BY d.monthly_sales DESC LIMIT ?", limit);
    }

    /** 各品类订单占比（饼图数据源） */
    public List<Map<String, Object>> categoryShare() {
        return jdbcTemplate.queryForList(
                "SELECT sc.name AS categoryName, COUNT(o.id) AS orderCount "
                        + "FROM shop s LEFT JOIN shop_category sc ON s.category_id = sc.id "
                        + "LEFT JOIN orders o ON o.shop_id = s.id AND o.deleted = 0 "
                        + "GROUP BY sc.name ORDER BY orderCount DESC");
    }

    /** 时段分布（柱状图数据源） */
    public List<Map<String, Object>> hourDistribution() {
        return jdbcTemplate.queryForList(
                "SELECT HOUR(create_time) AS hourOfDay, COUNT(*) AS orderCount "
                        + "FROM orders WHERE deleted = 0 GROUP BY HOUR(create_time) ORDER BY hourOfDay");
    }

    /** 平台用户反馈热词（AI 提取的评价标签） */
    public List<Map<String, Object>> reviewTagCloud(int limit) {
        return jdbcTemplate.queryForList(
                "SELECT tag_name AS tagName, COUNT(*) AS tagCount FROM review_tag "
                        + "GROUP BY tag_name ORDER BY tagCount DESC LIMIT ?", limit);
    }

    // ------------------------------------------------------------------

    private Long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private java.math.BigDecimal queryDecimal(String sql) {
        java.math.BigDecimal value = jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class);
        return value == null ? java.math.BigDecimal.ZERO : value;
    }
}
