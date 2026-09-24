package com.campus.delivery.modules.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.delivery.common.api.PageResult;
import com.campus.delivery.common.constant.RedisKeys;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.shop.entity.Merchant;
import com.campus.delivery.modules.shop.entity.Shop;
import com.campus.delivery.modules.shop.mapper.MerchantMapper;
import com.campus.delivery.modules.shop.mapper.ShopMapper;
import com.campus.delivery.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 店铺服务。主责：成员2。
 *
 * <p>要点：
 * <ul>
 *   <li>店铺列表按分类/销量/评分排序，热点数据缓存到 Redis；</li>
 *   <li>商户端所有操作通过 {@link #currentMerchantId()} 取本店，禁止前端传 merchantId。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 店铺列表（学生端首页） */
    public PageResult<Shop> pageShops(String categoryId, String keyword, String sortBy,
                                      long pageNum, long pageSize) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getBanStatus, 0)
                .eq(StringUtils.hasText(categoryId), Shop::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), Shop::getShopName, keyword);
        // 营业中的店铺优先展示
        wrapper.orderByDesc(Shop::getBusinessStatus);
        if ("score".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(Shop::getScore);
        } else {
            wrapper.orderByDesc(Shop::getMonthlySales);
        }

        Page<Shop> page = shopMapper.selectPage(Page.of(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 店铺详情（热点缓存，减少数据库压力） */
    public Shop getDetail(String shopId) {
        String cacheKey = RedisKeys.SHOP_DETAIL + shopId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Shop shop) {
            return shop;
        }
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(20001, "店铺不存在");
        }
        redisTemplate.opsForValue().set(cacheKey, shop, 10, TimeUnit.MINUTES);
        return shop;
    }

    /** 当前登录商户的店铺（商户端入口） */
    public Shop getMyShop() {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, currentMerchantId())
                .last("LIMIT 1"));
    }

    /**
     * 当前登录商户的店铺ID。
     *
     * <p>供菜品、订单、评价等商户端模块复用，避免各处重复解析商户归属。
     */
    public String currentShopId() {
        Shop shop = getMyShop();
        if (shop == null) {
            throw new BusinessException(20002, "尚未创建店铺，请联系管理员");
        }
        return shop.getId();
    }

    /** 维护店铺信息 */
    @Transactional(rollbackFor = Exception.class)
    public Shop updateMyShop(Shop form) {
        Shop shop = getMyShop();
        if (shop == null) {
            throw new BusinessException(20002, "尚未创建店铺，请联系管理员");
        }
        // 只允许修改本店的可编辑字段，主键与归属字段不可改
        shop.setShopName(form.getShopName());
        shop.setDescription(form.getDescription());
        shop.setAddress(form.getAddress());
        shop.setPhone(form.getPhone());
        shop.setNotice(form.getNotice());
        shop.setOpenTime(form.getOpenTime());
        shop.setMinPrice(form.getMinPrice());
        shop.setDeliveryFee(form.getDeliveryFee());
        shop.setDeliveryTime(form.getDeliveryTime());
        shop.setLogo(form.getLogo());
        shopMapper.updateById(shop);
        evictCache(shop.getId());
        return shop;
    }

    /** 设置营业状态：0休息中 1营业中 */
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessStatus(Integer businessStatus) {
        Shop shop = getMyShop();
        if (shop == null) {
            throw new BusinessException(20002, "尚未创建店铺，请联系管理员");
        }
        shop.setBusinessStatus(businessStatus);
        shopMapper.updateById(shop);
        evictCache(shop.getId());
    }

    /**
     * 当前登录商户对应的商户ID。
     *
     * <p>数据行级隔离的关键：所有商户端接口都通过本方法取商户，不接受前端传入。
     */
    public String currentMerchantId() {
        String userId = UserContext.getUserId();
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId).last("LIMIT 1"));
        if (merchant == null) {
            throw new BusinessException(20003, "当前账号未绑定商户信息");
        }
        if (merchant.getAuditStatus() == null || merchant.getAuditStatus() != 1) {
            throw new BusinessException(20004, "商户尚未通过审核");
        }
        return merchant.getId();
    }

    /**
     * 店铺所属商户的登录账号ID。
     *
     * <p>供订单模块做「新订单推送给商户」时使用，避免跨模块访问商户 Mapper。
     */
    public String getMerchantUserId(String shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(20001, "店铺不存在");
        }
        Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
        return merchant == null ? null : merchant.getUserId();
    }

    private void evictCache(String shopId) {
        redisTemplate.delete(RedisKeys.SHOP_DETAIL + shopId);
    }
}
