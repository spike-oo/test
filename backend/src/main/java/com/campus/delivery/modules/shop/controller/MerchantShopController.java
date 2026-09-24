package com.campus.delivery.modules.shop.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.shop.entity.Shop;
import com.campus.delivery.modules.shop.service.ShopService;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.RequiresRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户端店铺管理接口。主责：成员2。
 */
@Tag(name = "04-商户端·店铺管理", description = "店铺信息维护、营业状态设置")
@RestController
@RequestMapping("/merchant/shop")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.MERCHANT)
public class MerchantShopController {

    private final ShopService shopService;

    @Operation(summary = "我的店铺")
    @GetMapping
    public Result<Shop> myShop() {
        return Result.ok(shopService.getMyShop());
    }

    @Operation(summary = "维护店铺信息")
    @PutMapping
    public Result<Shop> update(@RequestBody Shop form) {
        return Result.ok(shopService.updateMyShop(form));
    }

    @Operation(summary = "设置营业状态", description = "0休息中 1营业中")
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestParam Integer businessStatus) {
        shopService.updateBusinessStatus(businessStatus);
        return Result.ok();
    }
}
