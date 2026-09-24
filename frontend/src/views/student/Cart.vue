<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>购物车</template>
      <!-- TODO(成员1)：GET /cart 渲染购物车；勾选、改数量、删除；底部结算 -->
      <el-empty description="待实现：购物车列表与结算入口" />
      <div class="summary text-muted">
        已选 {{ cartStore.selectedCount }} 件，合计 ￥{{ cartStore.selectedAmount.toFixed(2) }}
      </div>
      <el-button type="primary" :disabled="cartStore.selectedCount === 0" @click="onCheckout">去结算</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

onMounted(() => cartStore.refresh())

// TODO(成员1)：跳转确认订单页，POST /orders 提交（同一订单只能包含同一店铺的菜品）
function onCheckout() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员1): checkout')
}
</script>

<style scoped>
.summary {
  margin: 12px 0;
}
</style>
