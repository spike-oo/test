package com.campus.delivery.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.enums.CommonEnums;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.admin.entity.Ticket;
import com.campus.delivery.modules.admin.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 客服工单服务。主责：成员3。
 *
 * <p>闭环：AI 客服无法回答 → 自动建单 → 管理员处理 → 关闭工单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private static final DateTimeFormatter TICKET_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TicketMapper ticketMapper;

    /** AI 客服转人工：自动建单 */
    @Transactional(rollbackFor = Exception.class)
    public Ticket createFromChat(String userId, String sessionId, String orderId, String question) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo("TK" + LocalDateTime.now().format(TICKET_NO_FORMATTER)
                + String.format("%03d", (int) (Math.random() * 1000)));
        ticket.setUserId(userId);
        ticket.setSessionId(sessionId);
        ticket.setOrderId(orderId);
        ticket.setCategory(guessCategory(question));
        ticket.setQuestion(question);
        ticket.setStatus(CommonEnums.TicketStatus.WAITING.getCode());
        ticketMapper.insert(ticket);
        log.info("AI 客服转人工，工单已创建: ticketNo={}, userId={}", ticket.getTicketNo(), userId);
        return ticket;
    }

    /** 工单列表（管理端） */
    public PageResult<Ticket> page(Integer status, long pageNum, long pageSize) {
        Page<Ticket> page = ticketMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Ticket>()
                        .eq(status != null, Ticket::getStatus, status)
                        .orderByAsc(Ticket::getStatus)
                        .orderByDesc(Ticket::getCreateTime));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 我的工单（学生端查看处理进度） */
    public List<Ticket> listMine(String userId) {
        return ticketMapper.selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getUserId, userId)
                .orderByDesc(Ticket::getCreateTime));
    }

    /** 管理员处理工单 */
    @Transactional(rollbackFor = Exception.class)
    public void handle(String ticketId, String handlerId, String reply, boolean close) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(70001, "工单不存在");
        }
        if (CommonEnums.TicketStatus.CLOSED.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(70002, "工单已关闭");
        }
        ticket.setHandlerId(handlerId);
        if (StringUtils.hasText(reply)) {
            ticket.setReply(reply);
        }
        if (close) {
            ticket.setStatus(CommonEnums.TicketStatus.CLOSED.getCode());
            ticket.setCloseTime(LocalDateTime.now());
        } else {
            ticket.setStatus(CommonEnums.TicketStatus.PROCESSING.getCode());
        }
        ticketMapper.updateById(ticket);
    }

    /** 待处理工单数（数据看板） */
    public long countWaiting() {
        Long count = ticketMapper.selectCount(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getStatus, CommonEnums.TicketStatus.WAITING.getCode()));
        return count == null ? 0L : count;
    }

    /** 按关键词粗略分类，便于工单分配 */
    private String guessCategory(String question) {
        if (!StringUtils.hasText(question)) {
            return "其他";
        }
        if (question.contains("退款") || question.contains("退钱")) {
            return "退款";
        }
        if (question.contains("配送") || question.contains("骑手") || question.contains("多久")) {
            return "配送";
        }
        if (question.contains("账号") || question.contains("密码") || question.contains("登录")) {
            return "账号";
        }
        if (question.contains("订单") || question.contains("取消") || question.contains("餐")) {
            return "订单";
        }
        return "其他";
    }
}
