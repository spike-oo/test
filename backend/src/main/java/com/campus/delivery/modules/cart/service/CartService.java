package com.campus.delivery.modules.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.modules.cart.dto.AddCartRequest;
import com.campus.delivery.modules.cart.dto.CartItemVO;
import com.campus.delivery.modules.cart.entity.CartItem;
import com.campus.delivery.modules.cart.mapper.CartItemMapper;
import com.campus.delivery.modules.dish.entity.Dish;
import com.campus.delivery.modules.dish.service.DishService;
import com.campus.delivery.modules.shop.entity.Shop;
import com.campus.delivery.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务。主责：成员1。
 *
 * <p>跨模块调用约定：购物车需要菜品与店铺信息，通过 {@link DishService} 与
 * {@link ShopService} 获取，不直接访问对方的 Mapper（见 docs/02-四人分工与模块归属.md）。
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final int CODE_DISH_UNAVAILABLE = 30010;
    private static final int CODE_SHOP_MISMATCH = 30011;
    private static final int CODE_CART_ITEM_NOT_FOUND = 30012;

    private final CartItemMapper cartItemMapper;
    private final DishService dishService;
    private final ShopService shopService;

    /** 购物车列表：附带菜品快照与金额小计 */
    public List<CartItemVO> listCart(String userId) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime));

        List<CartItemVO> result = new ArrayList<>(items.size());
        for (CartItem item : items) {
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setShopId(item.getShopId());
            vo.setDishId(item.getDishId());
            vo.setSpecId(item.getSpecId());
            vo.setQuantity(item.getQuantity());
            vo.setSelected(item.getSelected());

            Dish dish = dishService.getDetail(item.getDishId());
            vo.setDishName(dish.getDishName());
            vo.setDishImage(dish.getImage());
            vo.setPrice(dish.getPrice());
            vo.setStock(dish.getStock());
            vo.setDishStatus(dish.getStatus());
            vo.setAmount(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            Shop shop = shopService.getDetail(item.getShopId());
            vo.setShopName(shop.getShopName());
            result.add(vo);
        }
        return result;
    }

    /** 加入购物车：同店铺约束 + 同菜品同规格合并数量 */
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(String userId, AddCartRequest request) {
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity <= 0) {
            throw new BusinessException(30013, "数量必须大于 0");
        }

        Dish dish = dishService.getDetail(request.getDishId());
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BusinessException(CODE_DISH_UNAVAILABLE, "菜品已下架，无法加入购物车");
        }

        // 购物车内已有其他店铺的菜品时给出提示，避免结算时才发现（需求文档 5.1.4）
        List<CartItem> exists = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));
        boolean otherShop = exists.stream().anyMatch(i -> !i.getShopId().equals(dish.getShopId()));
        if (otherShop) {
            throw new BusinessException(CODE_SHOP_MISMATCH, "购物车中已有其他店铺的菜品，请先结算或清空");
        }

        CartItem same = exists.stream()
                .filter(i -> i.getDishId().equals(request.getDishId())
                        && java.util.Objects.equals(i.getSpecId(), request.getSpecId()))
                .findFirst().orElse(null);

        if (same != null) {
            int newQuantity = same.getQuantity() + quantity;
            if (dish.getStock() != null && newQuantity > dish.getStock()) {
                throw new BusinessException(30014, "库存不足，当前仅剩 " + dish.getStock() + " 份");
            }
            same.setQuantity(newQuantity);
            cartItemMapper.updateById(same);
            return;
        }

        if (dish.getStock() != null && quantity > dish.getStock()) {
            throw new BusinessException(30014, "库存不足，当前仅剩 " + dish.getStock() + " 份");
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setShopId(dish.getShopId());
        item.setDishId(request.getDishId());
        item.setSpecId(request.getSpecId());
        item.setQuantity(quantity);
        item.setSelected(1);
        cartItemMapper.insert(item);
    }

    /** 调整数量 */
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(String userId, String cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(30013, "数量必须大于 0");
        }
        CartItem item = requireOwnItem(userId, cartItemId);
        Dish dish = dishService.getDetail(item.getDishId());
        if (dish.getStock() != null && quantity > dish.getStock()) {
            throw new BusinessException(30014, "库存不足，当前仅剩 " + dish.getStock() + " 份");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    /** 勾选/取消勾选 */
    @Transactional(rollbackFor = Exception.class)
    public void updateSelected(String userId, String cartItemId, Integer selected) {
        CartItem item = requireOwnItem(userId, cartItemId);
        item.setSelected(selected);
        cartItemMapper.updateById(item);
    }

    /** 删除购物车项 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(String userId, String cartItemId) {
        requireOwnItem(userId, cartItemId);
        cartItemMapper.deleteById(cartItemId);
    }

    /** 清空购物车（下单成功后调用） */
    @Transactional(rollbackFor = Exception.class)
    public void clear(String userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    /** 取购物车中已勾选的项（下单时使用） */
    public List<CartItem> listSelected(String userId) {
        return cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));
    }

    private CartItem requireOwnItem(String userId, String cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException(CODE_CART_ITEM_NOT_FOUND, "购物车记录不存在或无权操作");
        }
        return item;
    }
}
