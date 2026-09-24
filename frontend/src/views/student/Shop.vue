<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>店铺详情 · 菜品列表</template>
      <!-- TODO(成员1)：GET /shops/{id} 渲染店铺信息；按分类渲染菜品列表 -->
      <el-empty description="待实现：店铺信息、菜品分类、加购物车按钮" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
// TODO(成员1)：const shopId = route.params.shopId as string，加载店铺与菜品
// eslint-disable-next-line no-console
console.log('TODO(成员1): shop', route.params.shopId)
</script>
