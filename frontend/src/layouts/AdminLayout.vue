<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="brand">管理后台</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/admin/dashboard">数据看板</el-menu-item>
        <el-menu-item index="/admin/merchants">商户审核</el-menu-item>
        <el-menu-item index="/admin/riders">骑手审核</el-menu-item>
        <el-menu-item index="/admin/knowledge">知识库</el-menu-item>
        <el-menu-item index="/admin/tickets">客服工单</el-menu-item>
        <el-menu-item index="/admin/config">系统配置</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ route.meta.title }}</span>
        <el-button text @click="onLogout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

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
</style>
