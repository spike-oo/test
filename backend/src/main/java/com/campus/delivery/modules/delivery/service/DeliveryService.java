package com.campus.delivery.modules.delivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.constant.RedisKeys;
import com.campus.delivery.common.enums.CommonEnums;
import com.campus.delivery.common.enums.OrderStatus;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.delivery.entity.Rider;
import com.campus.delivery.modules.delivery.mapper.GrabLogMapper;
import com.campus.delivery.modules.delivery.mapper.RiderMapper;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 配送服务：订单大厅、抢单、取餐、送达、收入统计。主责：成员4（P2 可选加分）。
 *
 * <p>抢单并发控制（需求文档 5.6.2 难点二）：
 * <ol>
 *   <li>Redis 分布式锁 {@code lock:order:grab:{orderId}} 保证同一订单同一时刻只有一个骑手进入；</li>
 *   <li>数据库条件更新（{@code rider_id IS NULL}）做最终一致性兜底；</li>
 *   <li>每次抢单尝试写入 {@code grab_log}，失败原因可追溯。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final int CODE_RIDER_NOT_FOUND = 40001;
    private static final int CODE_GRAB_FAILED = 40002;
    private static final int CODE_RIDER_RESTING = 40003;
    private static final int CODE_LIMIT_EXCEEDED = 40004;

    private static final long GRAB_LOCK_SECONDS = 10L;

    private final RiderMapper riderMapper;
    private final GrabLogMapper grabLogMapper;
    private final OrderService orderService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 订单大厅：可抢订单列表 */
    public PageResult<Order> orderHall(long pageNum, long pageSize) {
        return orderService.pageGrabPoolOrders(pageNum, pageSize);
    }

    /** 抢单 */
    @Transactional(rollbackFor = Exception.class)
    public Order grab(String riderId, String orderId) {
        String lockKey = RedisKeys.LOCK_ORDER_GRAB + orderId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, riderId, GRAB_LOCK_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            recordGrabLog(orderId, riderId, 0, "手慢了，订单已被其他骑手抢走");
            throw new BusinessException(CODE_GRAB_FAILED, "手慢了，订单已被其他骑手抢走");
        }

        try {
            Rider rider = requireRider(riderId);
            if (!CommonEnums.WorkStatus.ACCEPTING.getCode().equals(rider.getWorkStatus())) {
                recordGrabLog(orderId, riderId, 0, "工作状态为休息中");
                throw new BusinessException(CODE_RIDER_RESTING, "当前为休息中状态，请先切换为接单中");
            }
            if (rider.getDeliveringCount() != null
                    && rider.getMaxDelivering() != null
                    && rider.getDeliveringCount() >= rider.getMaxDelivering()) {
                recordGrabLog(orderId, riderId, 0, "超出接单上限");
                throw new BusinessException(CODE_LIMIT_EXCEEDED,
                        "您有配送中的订单，请先完成（上限 " + rider.getMaxDelivering() + " 单）");
            }

            // 绑定骑手：内部会再次校验订单状态与 rider_id 是否为空
            Order order = orderService.bindRider(orderId, riderId);

            int rows = riderMapper.increaseDelivering(riderId);
            if (rows == 0) {
                throw new BusinessException(CODE_LIMIT_EXCEEDED, "超出接单上限，抢单失败");
            }
            recordGrabLog(orderId, riderId, 1, null);
            log.info("抢单成功: riderId={}, orderId={}", riderId, orderId);
            return order;
        } catch (BusinessException e) {
            // 抢单失败时把订单重新放回抢单池，避免被锁期间其他人也抢不到
            redisTemplate.opsForZSet().add(RedisKeys.GRAB_POOL, orderId, System.currentTimeMillis());
            throw e;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /** 确认取餐：订单进入配送中 */
    @Transactional(rollbackFor = Exception.class)
    public Order pickup(String riderId, String orderId) {
        return orderService.riderTransit(orderId, riderId, OrderStatus.DELIVERING);
    }

    /** 确认送达：订单完成，骑手配送中数量 -1、收入累加 */
    @Transactional(rollbackFor = Exception.class)
    public Order deliver(String riderId, String orderId) {
        Order order = orderService.riderTransit(orderId, riderId, OrderStatus.FINISHED);
        riderMapper.decreaseDelivering(riderId, order.getDeliveryFee() == null
                ? BigDecimal.ZERO : order.getDeliveryFee());
        // TODO(成员4)：更新 delivery_record（取餐/送达时间、配送耗时、配送费）
        return order;
    }

    /** 我的配送订单 */
    public PageResult<Order> myDeliveringOrders(String riderId, Integer status, long pageNum, long pageSize) {
        return orderService.pageRiderOrders(riderId, status, pageNum, pageSize);
    }

    /** 收入统计：今日 / 本周 / 本月 */
    public Map<String, Object> income(String riderId) {
        Map<String, Object> stat = grabLogMapper.statIncome(riderId);
        stat.put("totalIncome", grabLogMapper.sumIncome(riderId));
        return stat;
    }

    /** 切换工作状态：0休息中 1接单中 */
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkStatus(String riderId, Integer workStatus) {
        Rider rider = requireRider(riderId);
        rider.setWorkStatus(workStatus);
        riderMapper.updateById(rider);
    }

    // ------------------------------------------------------------------

    /** 根据登录账号ID定位骑手档案 */
    public Rider getByUserId(String userId) {
        return riderMapper.selectOne(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUserId, userId).last("LIMIT 1"));
    }

    private Rider requireRider(String riderId) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new BusinessException(CODE_RIDER_NOT_FOUND, "骑手信息不存在");
        }
        if (!CommonEnums.AuditStatus.PASSED.getCode().equals(rider.getAuditStatus())) {
            throw new BusinessException(40005, "骑手资质尚未通过审核");
        }
        return rider;
    }

    private void recordGrabLog(String orderId, String riderId, int result, String failReason) {
        try {
            grabLogMapper.insert(UUID.randomUUID().toString().replace("-", ""),
                    orderId, riderId, result, failReason);
        } catch (Exception e) {
            log.warn("抢单日志写入失败: {}", e.getMessage());
        }
    }
}
