package com.campus.delivery.modules.admin.controller;

import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.api.Result;
import com.campus.delivery.modules.admin.entity.Ticket;
import com.campus.delivery.modules.admin.service.TicketService;
import com.campus.delivery.security.RequiresRole;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客服工单接口（管理端 + 学生端查看自己的工单）。主责：成员3。
 */
@Tag(name = "14-客服工单", description = "工单列表、处理、关闭")
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketService ticketService;

    @Operation(summary = "工单列表（管理端）")
    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public Result<PageResult<Ticket>> list(@RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(ticketService.page(status, pageNum, pageSize));
    }

    @Operation(summary = "我的工单", description = "学生端查看自己提交的客服工单")
    @GetMapping("/mine")
    public Result<java.util.List<Ticket>> mine() {
        return Result.ok(ticketService.listMine(UserContext.getUserId()));
    }

    @Operation(summary = "处理工单")
    @PostMapping("/{id}/handle")
    @RequiresRole(RoleEnum.ADMIN)
    public Result<Void> handle(@PathVariable String id,
                               @RequestParam String reply,
                               @RequestParam(defaultValue = "false") boolean close) {
        ticketService.handle(id, UserContext.getUserId(), reply, close);
        return Result.ok();
    }
}
