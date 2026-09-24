package com.campus.delivery.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.constant.RedisKeys;
import com.campus.delivery.common.enums.CommonEnums;
import com.campus.delivery.common.enums.OrderStatus;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.cart.entity.CartItem;
import com.campus.delivery.modules.cart.service.CartService;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.order.dto.CreateOrderRequest;
import com.campus.delivery.modules.order.dto.OrderVO;
import com.campus.delivery.modules.order.entity.Order;
import com.campus.delivery.modules.order.entity.OrderItem;
import com.campus.delivery.modules.order.entity.OrderStatusLog;
import com.campus.delivery.modules.order.mapper.OrderItemMapper;
import com.campus.delivery.modules.order.mapper.OrderMapper;
import com.campus.delivery.modules.order.mapper.OrderStatusLogMapper;
import com.campus.delivery.modules.shop.entity.Shop;
import com.campus.delivery.modules.shop.service.ShopService;
import com.campus.delivery.security.RoleEnum;
import com.campus.delivery.websocket.OrderNotifyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务。主责：成员4。
 *
 * <p>核心流程（需求文档 4.1 下单流程时序）：
 * <pre>
 *   提交订单 → 扣库存 → 生成订单与明细 → 模拟支付 → 记录初始状态 → 清空购物车 → 推送商户
 * </pre>
 *
 * <p>事务边界：扣库存、写订单、写明细、写状态日志在同一事务内，任一失败整体回滚
 * （需求文档 5.6.2 难点二）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int CODE_CART_EMPTY = 30020;
    private static final int CODE_SHOP_CLOSED = 30021;
    private static final int CODE_ORDER_NOT_FOUND = 30022;
    private static final int CODE_NO_PERMISSION = 30023;
    private static final int CODE_CANNOT_CANCEL = 30024;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderStateMachine orderStateMachine;
    private final CartService cartService;
    private final DishService dishService;
    private final ShopService shopService;
    private final OrderNotifyHandler orderNotifyHandler;
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================================================================
    // 学生端
    // ==================================================================

    /**
     * 提交订单（购物车结算 + 模拟支付）。
     *
     * <p>TODO(成员4)：把模拟支付落库到 {@code payment_record}，
     * 余额支付需扣减 {@code student.balance} 并做余额充足校验。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(String userId, CreateOrderRequest request) {
        List<CartItem> cartItems = cartService.listSelected(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(CODE_CART_EMPTY, "购物车中没有已勾选的商品");
        }

        String shopId = cartItems.get(0).getShopId();
        boolean multiShop = cartItems.stream().anyMatch(item -> !item.getShopId().equals(shopId));
        if (multiShop) {
            throw new BusinessException(30011, "同一订单只能包含同一店铺的菜品");
        }

        Shop shop = shopService.getDetail(shopId);
        if (shop.getBusinessStatus() == null || shop.getBusinessStatus() != 1) {
            throw new BusinessException(CODE_SHOP_CLOSED, "店铺休息中，暂不接单");
        }

        // 1) 扣减库存并生成明细快照（库存不足会抛异常，整体回滚）
        List<OrderItem> orderItems = new ArrayList<>(cartItems.size());
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Dish dish = dishService.getDetail(cartItem.getDishId());
            dishService.deductStock(cartItem.getDishId(), cartItem.getQuantity());

            BigDecimal amount = dish.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getDishName());
            orderItem.setDishImage(dish.getImage());
            orderItem.setPrice(dish.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setAmount(amount);
            orderItems.add(orderItem);
            goodsAmount = goodsAmount.add(amount);
        }

        // 2) 生成订单
        boolean selfPickup = Integer.valueOf(2).equals(request.getDeliveryType());
        BigDecimal deliveryFee = selfPickup ? BigDecimal.ZERO : defaultFee(shop.getDeliveryFee());

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setGoodsAmount(goodsAmount);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(goodsAmount.add(deliveryFee));
        order.setPayType(request.getPayType() == null ? 1 : request.getPayType());
        order.setPayStatus(CommonEnums.PayStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING_ACCEPT.getCode());
        order.setDeliveryType(request.getDeliveryType() == null ? 1 : request.getDeliveryType());
        order.setReceiver(request.getReceiver());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setAddress(request.getAddress());
        order.setRemark(request.getRemark());
        order.setVersion(0);
        orderMapper.insert(order);

        // 3) 订单明细
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        // 4) 初始状态流转记录
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(order.getId());
        statusLog.setFromStatus(null);
        statusLog.setToStatus(OrderStatus.PENDING_ACCEPT.getCode());
        statusLog.setOperatorId(userId);
        statusLog.setOperatorRole(RoleEnum.STUDENT.getCode());
        statusLog.setReason("学生提交订单");
        orderStatusLogMapper.insert(statusLog);

        // 5) 清空购物车
        cartService.clear(userId);

        // 6) 实时通知商户有新订单
        notifyMerchantNewOrder(shopId, order);

        log.info("订单创建成功: orderNo={}, userId={}, amount={}", order.getOrderNo(), userId, order.getTotalAmount());
        return buildOrderVO(order, orderItems);
    }

    /** 我的订单列表 */
    public PageResult<OrderVO> pageMyOrders(String userId, Integer status, long pageNum, long pageSize) {
        Page<Order> page = orderMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(status != null, Order::getOrderStatus, status)
                        .orderByDesc(Order::getCreateTime));
        List<OrderVO> list = new ArrayList<>(page.getRecords().size());
        for (Order order : page.getRecords()) {
            list.add(buildOrderVO(order, listItems(order.getId())));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    /** 订单详情（含越权校验） */
    public OrderVO getDetail(String orderId, String userId, String role) {
        Order order = requireOrder(orderId);
        if (RoleEnum.STUDENT.getCode().equals(role) && !userId.equals(order.getUserId())) {
            throw new BusinessException(CODE_NO_PERMISSION, "无权查看他人订单");
        }
        if (RoleEnum.RIDER.getCode().equals(role) && !userId.equals(order.getRiderId())) {
            throw new BusinessException(CODE_NO_PERMISSION, "无权查看未分配的订单");
        }
        return buildOrderVO(order, listItems(orderId));
    }

    /** 学生取消订单（仅待接单状态可取消，取消后回补库存） */
    @Transactional(rollbackFor = Exception.class)
    public void cancelByStudent(String userId, String orderId, String reason) {
        Order order = requireOrder(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(CODE_NO_PERMISSION, "无权操作他人订单");
        }
        if (!OrderStatus.of(order.getOrderStatus()).equals(OrderStatus.PENDING_ACCEPT)) {
            throw new BusinessException(CODE_CANNOT_CANCEL, "订单已被商家接单，无法直接取消，请联系商家或提交客服工单");
        }
        orderStateMachine.transit(order, OrderStatus.CANCELED, userId, RoleEnum.STUDENT.getCode(), reason);
        restoreStock(orderId);
        // TODO(成员4)：模拟支付退款，把 pay_status 置为已退款并写 payment_record
        orderNotifyHandler.sendToUser(userId, "ORDER_STATUS", statusPayload(order));
    }

    // ==================================================================
    // 商户端
    // ==================================================================

    /** 本店订单列表 */
    public PageResult<OrderVO> pageShopOrders(Integer status, long pageNum, long pageSize) {
        String shopId = shopService.currentShopId();
        Page<Order> page = orderMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getShopId, shopId)
                        .eq(status != null, Order::getOrderStatus, status)
                        .orderByDesc(Order::getCreateTime));
        List<OrderVO> list = new ArrayList<>(page.getRecords().size());
        for (Order order : page.getRecords()) {
            list.add(buildOrderVO(order, listItems(order.getId())));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    /**
     * 商户状态流转。
     *
     * @param target 1 接单（备餐中）/ 2 已出餐（待取餐，进入抢单池）/ 5 拒单（已取消）
     */
    @Transactional(rollbackFor = Exception.class)
    public Order merchantTransit(String orderId, Integer target, String reason) {
        Order order = requireOrder(orderId);
        if (!shopService.currentShopId().equals(order.getShopId())) {
            throw new BusinessException(CODE_NO_PERMISSION, "无权操作其他店铺的订单");
        }
        OrderStatus targetStatus = OrderStatus.of(target);
        if (targetStatus != OrderStatus.PREPARING
                && targetStatus != OrderStatus.PENDING_PICKUP
                && targetStatus != OrderStatus.CANCELED) {
            throw new BusinessException(30025, "商户只能执行接单、出餐、拒单操作");
        }

        OrderStatus before = OrderStatus.of(order.getOrderStatus());
        order = orderStateMachine.transit(order, targetStatus, shopService.getMerchantUserId(order.getShopId()),
                RoleEnum.MERCHANT.getCode(), reason);

        // 出餐：生成取餐码并进入骑手抢单池
        if (targetStatus == OrderStatus.PENDING_PICKUP) {
            order.setPickupCode(generatePickupCode());
            orderMapper.updateById(order);
            pushToGrabPool(order);
        }
        // 拒单：回补库存并退款
        if (targetStatus == OrderStatus.CANCELED) {
            restoreStock(orderId);
            // TODO(成员4)：模拟支付退款，把 pay_status 置为已退款
        }

        notifyOrderStatus(order, before);
        return order;
    }

    // ==================================================================
    // 骑手端（由 delivery 模块调用）
    // ==================================================================

    /** 骑手取餐 / 送达 */
    @Transactional(rollbackFor = Exception.class)
    public Order riderTransit(String orderId, String riderId, OrderStatus target) {
        Order order = requireOrder(orderId);
        if (riderId != null && !riderId.equals(order.getRiderId())) {
            throw new BusinessException(CODE_NO_PERMISSION, "该订单未分配给当前骑手");
        }
        if (target != OrderStatus.DELIVERING && target != OrderStatus.FINISHED) {
            throw new BusinessException(30026, "骑手只能执行取餐、送达操作");
        }
        OrderStatus before = OrderStatus.of(order.getOrderStatus());
        order = orderStateMachine.transit(order, target, riderId, RoleEnum.RIDER.getCode(), null);

        if (target == OrderStatus.DELIVERING) {
            removeFromGrabPool(orderId);
        }
        notifyOrderStatus(order, before);
        return order;
    }

    /** 查询订单实体（供 delivery 模块抢单校验使用） */
    public Order requireOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(CODE_ORDER_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    /**
     * 数据看板：近 N 天订单量与销售额趋势（供成员3的管理端调用）。
     *
     * @return 每项含 stat_date / order_count / sales_amount
     */
    public List<Map<String, Object>> dailyTrend(int days) {
        return orderMapper.statDailyTrend(days);
    }

    /** 平台级订单总量（数据看板核心指标） */
    public long countAll() {
        Long total = orderMapper.selectCount(null);
        return total == null ? 0L : total;
    }

    /** 骑手的历史配送订单 */
    public PageResult<Order> pageRiderOrders(String riderId, Integer status, long pageNum, long pageSize) {
        Page<Order> page = orderMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getRiderId, riderId)
                        .eq(status != null, Order::getOrderStatus, status)
                        .orderByDesc(Order::getCreateTime));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 骑手订单大厅：待取餐且尚未被抢的订单。
     *
     * <p>数据来源以数据库为准，Redis 抢单池（{@code RedisKeys.GRAB_POOL}）只用于
     * 快速判断与广播；缓存失效时可直接回源数据库。
     */
    public PageResult<Order> pageGrabPoolOrders(long pageNum, long pageSize) {
        Page<Order> page = orderMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderStatus, OrderStatus.PENDING_PICKUP.getCode())
                        .isNull(Order::getRiderId)
                        .eq(Order::getDeliveryType, 1)
                        .orderByAsc(Order::getCreateTime));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 抢单成功后绑定骑手。
     *
     * <p>状态保持「待取餐」，等骑手到店「确认取餐」后才进入「配送中」，
     * 与订单状态机 {@link OrderStatus#allowedNext()} 的约束保持一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order bindRider(String orderId, String riderId) {
        Order order = requireOrder(orderId);
        if (!OrderStatus.PENDING_PICKUP.equals(OrderStatus.of(order.getOrderStatus()))) {
            throw new BusinessException(40002, "手慢了，订单已被抢走或状态已变更");
        }
        if (order.getRiderId() != null) {
            throw new BusinessException(40002, "手慢了，订单已被其他骑手抢走");
        }
        order.setRiderId(riderId);
        orderMapper.updateById(order);
        removeFromGrabPool(orderId);

        Order latest = requireOrder(orderId);
        orderNotifyHandler.sendToUser(latest.getUserId(), "ORDER_STATUS", statusPayload(latest));
        String merchantUserId = shopService.getMerchantUserId(latest.getShopId());
        if (merchantUserId != null) {
            orderNotifyHandler.sendToUser(merchantUserId, "ORDER_STATUS", statusPayload(latest));
        }
        return latest;
    }

    // ==================================================================
    // 内部方法
    // ==================================================================

    private List<OrderItem> listItems(String orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    private OrderVO buildOrderVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(items);
        vo.setStatusDesc(OrderStatus.of(order.getOrderStatus()).getDesc());
        return vo;
    }

    private String generateOrderNo() {
        return "OD" + LocalDateTime.now().format(ORDER_NO_FORMATTER)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /** 取餐码：6 位数字，商户出餐时生成（需求文档 7.3.1） */
    private String generatePickupCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private BigDecimal defaultFee(BigDecimal fee) {
        return fee == null ? BigDecimal.ZERO : fee;
    }

    /** 回补订单内全部菜品库存 */
    private void restoreStock(String orderId) {
        for (OrderItem item : listItems(orderId)) {
            dishService.restoreStock(item.getDishId(), item.getQuantity());
        }
    }

    private void notifyMerchantNewOrder(String shopId, Order order) {
        String merchantUserId = shopService.getMerchantUserId(shopId);
        if (merchantUserId != null) {
            orderNotifyHandler.sendToUser(merchantUserId, "NEW_ORDER", statusPayload(order));
        }
    }

    private void notifyOrderStatus(Order order, OrderStatus before) {
        Map<String, Object> payload = statusPayload(order);
        payload.put("beforeStatus", before.getCode());
        orderNotifyHandler.sendToUser(order.getUserId(), "ORDER_STATUS", payload);
        String merchantUserId = shopService.getMerchantUserId(order.getShopId());
        if (merchantUserId != null) {
            orderNotifyHandler.sendToUser(merchantUserId, "ORDER_STATUS", payload);
        }
        if (order.getRiderId() != null) {
            orderNotifyHandler.sendToUser(order.getRiderId(), "ORDER_STATUS", payload);
        }
    }

    private Map<String, Object> statusPayload(Order order) {
        Map<String, Object> payload = new HashMap<>(6);
        payload.put("orderId", order.getId());
        payload.put("orderNo", order.getOrderNo());
        payload.put("status", order.getOrderStatus());
        payload.put("statusDesc", OrderStatus.of(order.getOrderStatus()).getDesc());
        return payload;
    }

    /** 订单进入骑手抢单池（Redis 有序集合，避免高频读压库） */
    private void pushToGrabPool(Order order) {
        if (!Integer.valueOf(1).equals(order.getDeliveryType())) {
            return; // 自取订单不进入抢单池
        }
        redisTemplate.opsForZSet().add(RedisKeys.GRAB_POOL, order.getId(),
                System.currentTimeMillis());
        orderNotifyHandler.broadcastToRole(RoleEnum.RIDER.getCode(), "GRAB_ORDER", statusPayload(order));
    }

    private void removeFromGrabPool(String orderId) {
        redisTemplate.opsForZSet().remove(RedisKeys.GRAB_POOL, orderId);
    }
}
