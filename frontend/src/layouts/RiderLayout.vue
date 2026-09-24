<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">骑手端</div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="menu">
        <el-menu-item index="/rider/hall">抢单大厅</el-menu-item>
        <el-menu-item index="/rider/delivering">配送中</el-menu-item>
        <el-menu-item index="/rider/history">历史订单</el-menu-item>
        <el-menu-item index="/rider/income">我的收入</el-menu-item>
        <el-menu-item index="/rider/profile">个人中心</el-menu-item>
      </el-menu>
      <div class="actions">
        <el-switch
          v-model="online"
          active-text="在线"
          inactive-text="离线"
          @change="onWorkStatusChange"
        />
        <el-button text @click="onLogout">退出</el-button>
      </div>
    </el-header>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { updateWorkStatus } from '@/api/rider'
import { useUserStore } from '@/store/user'
import { OrderSocket } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const socket = new OrderSocket()
const online = ref(true)

const activeMenu = computed(() => route.path)

onMounted(() => {
  // 新订单进入抢单池时广播给所有在线骑手
  socket.on('GRAB_ORDER', (data) => {
    ElNotification({ title: '有新订单可抢', message: `订单号 ${data?.orderNo || ''}`, type: 'success' })
  })
  socket.connect()
})

onUnmounted(() => socket.close())

async function onWorkStatusChange(value: boolean | string | number) {
  const status = value ? 1 : 0
  try {
    await updateWorkStatus(status)
    ElMessage.success(status === 1 ? '已上线，可以接单了' : '已离线，不再接收新单提醒')
  } catch {
    online.value = !status
  }
}

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
  font-weight: 600;
  color: var(--campus-primary);
  white-space: nowrap;
}
.menu {
  flex: 1;
  border-bottom: none;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
