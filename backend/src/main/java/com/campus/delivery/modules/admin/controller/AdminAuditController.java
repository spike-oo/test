package com.campus.delivery.modules.admin.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户与骑手审核接口。主责：成员3（数据写入由对应业务域负责）。
 */
@Tag(name = "16-管理端·审核与处置", description = "商户审核、骑手审核、违规处置")
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.ADMIN)
public class AdminAuditController {

    @Operation(summary = "审核商户", description = "pass=true 通过，false 驳回")
    @PostMapping("/merchant/{id}")
    public Result<Void> auditMerchant(@PathVariable String id,
                                      @RequestParam boolean pass,
                                      @RequestParam(required = false) String remark) {
        // TODO(成员3)：调用 ShopService.auditMerchant(id, pass, remark, UserContext.getUserId())
        return Result.ok();
    }

    @Operation(summary = "审核骑手")
    @PostMapping("/rider/{id}")
    public Result<Void> auditRider(@PathVariable String id,
                                   @RequestParam boolean pass,
                                   @RequestParam(required = false) String remark) {
        // TODO(成员3)：调用 DeliveryService.auditRider(id, pass, remark)
        return Result.ok();
    }

    @Operation(summary = "封禁/解封店铺")
    @PostMapping("/shop/{id}/ban")
    public Result<Void> banShop(@PathVariable String id, @RequestParam boolean ban) {
        // TODO(成员3)：调用 ShopService.updateBanStatus(id, ban)
        return Result.ok();
    }

    @Operation(summary = "封禁/解封骑手")
    @PostMapping("/rider/{id}/ban")
    public Result<Void> banRider(@PathVariable String id, @RequestParam boolean ban) {
        // TODO(成员3)：调用 DeliveryService.updateAuditStatus / 封禁逻辑
        return Result.ok();
    }
}
