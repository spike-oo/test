<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="brand">商户工作台</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/merchant/dashboard">经营概览</el-menu-item>
        <el-menu-item index="/merchant/orders">订单处理</el-menu-item>
        <el-menu-item index="/merchant/dishes">菜品管理</el-menu-item>
        <el-menu-item index="/merchant/reviews">评价管理</el-menu-item>
        <el-menu-item index="/merchant/ai-report">AI 经营助手</el-menu-item>
        <el-menu-item index="/merchant/shop">店铺设置</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ route.meta.title }}</span>
        <div class="actions">
          <span class="text-muted">新订单会通过 WebSocket 实时提醒</span>
          <el-button text @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { useUserStore } from '@/store/user'
import { OrderSocket } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const socket = new OrderSocket()

const activeMenu = computed(() => route.path)

onMounted(() => {
  socket.on('NEW_ORDER', (data) => {
    ElNotification({ title: '有新订单', message: `订单号 ${data?.orderNo || ''}，请及时接单`, type: 'warning' })
  })
  socket.connect()
})

onUnmounted(() => socket.close())

async function onLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
}
.brand {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: 600;
  color: var(--campus-primary);
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
