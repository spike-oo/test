package com.campus.delivery.common.constant;

/**
 * Redis Key 统一定义，禁止在各模块中散落硬编码字符串。
 *
 * <p>命名规范：{@code 业务:子业务:标识}，冒号分层，便于 {@code SCAN} 与运维排查。
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 登录 Token 黑名单（退出登录时写入，值为过期时间戳） */
    public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";

    /** 角色权限缓存：auth:perm:{role} */
    public static final String ROLE_PERMISSION = "auth:perm:";

    /** 热门店铺缓存（有序集合） */
    public static final String HOT_SHOP = "cache:shop:hot";

    /** 热门菜品缓存（有序集合） */
    public static final String HOT_DISH = "cache:dish:hot";

    /** 菜品详情缓存：cache:dish:detail:{dishId} */
    public static final String DISH_DETAIL = "cache:dish:detail:";

    /** 店铺详情缓存：cache:shop:detail:{shopId} */
    public static final String SHOP_DETAIL = "cache:shop:detail:";

    /** 骑手抢单池（有序集合，score 为出餐时间，保证先出餐先被看到） */
    public static final String GRAB_POOL = "delivery:grab:pool";

    /** 抢单分布式锁：lock:order:grab:{orderId} */
    public static final String LOCK_ORDER_GRAB = "lock:order:grab:";

    /** 下单扣库存分布式锁：lock:dish:stock:{dishId} */
    public static final String LOCK_DISH_STOCK = "lock:dish:stock:";

    /** 骑手在线状态：rider:online:{riderId}，值为最后心跳时间戳 */
    public static final String RIDER_ONLINE = "rider:online:";

    /** 骑手当前配送单数：rider:delivering:{riderId} */
    public static final String RIDER_DELIVERING_COUNT = "rider:delivering:";

    /** 客服对话上下文：ai:chat:context:{sessionId} */
    public static final String AI_CHAT_CONTEXT = "ai:chat:context:";

    /** 推荐结果缓存：ai:recommend:{userId}:{scene} */
    public static final String AI_RECOMMEND = "ai:recommend:";
}
