<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">
        <span class="logo">校园外卖</span>
        <span class="text-muted">学生端</span>
      </div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="menu">
        <el-menu-item index="/student/home">首页</el-menu-item>
        <el-menu-item index="/student/ai-order">AI 点餐</el-menu-item>
        <el-menu-item index="/student/search">搜索</el-menu-item>
        <el-menu-item index="/student/cart">购物车</el-menu-item>
        <el-menu-item index="/student/orders">我的订单</el-menu-item>
        <el-menu-item index="/student/diet-report">饮食分析</el-menu-item>
        <el-menu-item index="/student/chat">智能客服</el-menu-item>
        <el-menu-item index="/student/profile">个人中心</el-menu-item>
      </el-menu>
      <div class="actions">
        <el-badge :value="cartStore.items.length" :hidden="cartStore.items.length === 0">
          <el-button text @click="router.push('/student/cart')">购物车</el-button>
        </el-badge>
        <el-button text @click="onLogout">退出</el-button>
      </div>
    </el-header>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { OrderSocket } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const socket = new OrderSocket()

const activeMenu = computed(() => route.path)

onMounted(() => {
  cartStore.refresh()
  // 学生端只关心自己订单的状态变化
  socket.on('ORDER_STATUS', () => cartStore.refresh())
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
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.brand {
  display: flex;
  align-items: baseline;
  gap: 8px;
  white-space: nowrap;
}
.logo {
  font-size: 16px;
  font-weight: 600;
  color: var(--campus-primary);
}
.menu {
  flex: 1;
  border-bottom: none;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
