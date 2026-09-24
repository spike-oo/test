package com.campus.delivery.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.admin.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服工单数据访问。主责：成员3。
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
