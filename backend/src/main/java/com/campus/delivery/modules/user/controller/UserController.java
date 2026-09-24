package com.campus.delivery.modules.user.controller;

import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.user.dto.AddressDTO;
import com.campus.delivery.modules.user.dto.DietProfileDTO;
import com.campus.delivery.modules.user.dto.UserProfileVO;
import com.campus.delivery.modules.user.entity.DietProfile;
import com.campus.delivery.modules.user.entity.UserAddress;
import com.campus.delivery.modules.user.service.UserService;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人中心接口。主责：成员1。
 *
 * <p>所有接口从 {@link UserContext} 取当前登录人，不接受前端传入 userId，避免越权。
 */
@Tag(name = "02-个人中心", description = "个人信息、饮食档案、收货地址")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前登录人信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.ok(userService.getProfile(UserContext.getUserId()));
    }

    @Operation(summary = "查询饮食档案")
    @GetMapping("/diet-profile")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<DietProfile> dietProfile() {
        return Result.ok(userService.getOrCreateDietProfile(UserContext.getUserId()));
    }

    @Operation(summary = "维护饮食档案", description = "设置忌口、过敏原、饮食目标、口味偏好")
    @PutMapping("/diet-profile")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<DietProfile> updateDietProfile(@RequestBody DietProfileDTO dto) {
        return Result.ok(userService.updateDietProfile(UserContext.getUserId(), dto));
    }

    @Operation(summary = "收货地址列表")
    @GetMapping("/address")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<List<UserAddress>> addressList() {
        return Result.ok(userService.listAddress(UserContext.getUserId()));
    }

    @Operation(summary = "新增或修改收货地址")
    @PostMapping("/address")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<UserAddress> saveAddress(@Valid @RequestBody AddressDTO dto) {
        return Result.ok(userService.saveAddress(UserContext.getUserId(), dto));
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/address/{id}")
    @RequiresRole(RoleEnum.STUDENT)
    public Result<Void> deleteAddress(@PathVariable String id) {
        userService.deleteAddress(UserContext.getUserId(), id);
        return Result.ok();
    }
}
