<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>菜品详情</template>
      <!-- TODO(成员1)：GET /dishes/{id} 渲染菜品图片、价格、库存、标签、食材 -->
      <el-empty description="待实现：菜品详情、规格选择、加入购物车" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
// TODO(成员1)：加载菜品详情；POST /cart 加入购物车（同店约束由后端校验）
// eslint-disable-next-line no-console
console.log('TODO(成员1): dish', route.params.dishId)
</script>
