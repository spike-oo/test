package com.campus.delivery.modules.admin.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.admin.service.DashboardService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端数据看板接口。主责：成员3。
 */
@Tag(name = "13-管理端·数据看板", description = "核心指标、趋势、排行、占比")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.ADMIN)
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "核心指标")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(dashboardService.overview());
    }

    @Operation(summary = "订单与销售额趋势")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.ok(dashboardService.orderTrend(days));
    }

    @Operation(summary = "店铺销量排行")
    @GetMapping("/shop-ranking")
    public Result<List<Map<String, Object>>> shopRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.shopRanking(limit));
    }

    @Operation(summary = "菜品销量排行")
    @GetMapping("/dish-ranking")
    public Result<List<Map<String, Object>>> dishRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.dishRanking(limit));
    }

    @Operation(summary = "品类订单占比")
    @GetMapping("/category-share")
    public Result<List<Map<String, Object>>> categoryShare() {
        return Result.ok(dashboardService.categoryShare());
    }

    @Operation(summary = "时段分布")
    @GetMapping("/hour-distribution")
    public Result<List<Map<String, Object>>> hourDistribution() {
        return Result.ok(dashboardService.hourDistribution());
    }

    @Operation(summary = "用户反馈热词", description = "AI 提取的全平台评价标签词频")
    @GetMapping("/tag-cloud")
    public Result<List<Map<String, Object>>> tagCloud(@RequestParam(defaultValue = "30") int limit) {
        return Result.ok(dashboardService.reviewTagCloud(limit));
    }
}
