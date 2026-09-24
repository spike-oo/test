import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

import { ROLE, ROLE_HOME, useUserStore } from '@/store/user'

/**
 * 路由分区（需求文档 3.2）：
 *   /student  学生端
 *   /merchant 商户端
 *   /rider    骑手端
 *   /admin    管理员端
 *
 * meta.role 为必填访问角色，路由守卫据此做角色隔离。
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },

  // ============================ 学生端 ============================
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    redirect: '/student/home',
    meta: { role: ROLE.STUDENT },
    children: [
      { path: 'home', name: 'StudentHome', component: () => import('@/views/student/Home.vue'), meta: { title: '首页' } },
      { path: 'search', name: 'StudentSearch', component: () => import('@/views/student/Search.vue'), meta: { title: '搜索' } },
      { path: 'ai-order', name: 'StudentAiOrder', component: () => import('@/views/student/AiOrder.vue'), meta: { title: 'AI 点餐' } },
      { path: 'shop/:shopId', name: 'StudentShop', component: () => import('@/views/student/Shop.vue'), meta: { title: '店铺' } },
      { path: 'dish/:dishId', name: 'StudentDish', component: () => import('@/views/student/DishDetail.vue'), meta: { title: '菜品详情' } },
      { path: 'cart', name: 'StudentCart', component: () => import('@/views/student/Cart.vue'), meta: { title: '购物车' } },
      { path: 'orders', name: 'StudentOrders', component: () => import('@/views/student/Orders.vue'), meta: { title: '我的订单' } },
      { path: 'orders/:orderId', name: 'StudentOrderDetail', component: () => import('@/views/student/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'diet-report', name: 'StudentDietReport', component: () => import('@/views/student/DietReport.vue'), meta: { title: '饮食分析' } },
      { path: 'chat', name: 'StudentChat', component: () => import('@/views/student/Chat.vue'), meta: { title: '智能客服' } },
      { path: 'profile', name: 'StudentProfile', component: () => import('@/views/student/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },

  // ============================ 商户端 ============================
  {
    path: '/merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    redirect: '/merchant/dashboard',
    meta: { role: ROLE.MERCHANT },
    children: [
      { path: 'dashboard', name: 'MerchantDashboard', component: () => import('@/views/merchant/Dashboard.vue'), meta: { title: '经营概览' } },
      { path: 'shop', name: 'MerchantShop', component: () => import('@/views/merchant/ShopSetting.vue'), meta: { title: '店铺设置' } },
      { path: 'dishes', name: 'MerchantDishes', component: () => import('@/views/merchant/Dishes.vue'), meta: { title: '菜品管理' } },
      { path: 'orders', name: 'MerchantOrders', component: () => import('@/views/merchant/Orders.vue'), meta: { title: '订单处理' } },
      { path: 'reviews', name: 'MerchantReviews', component: () => import('@/views/merchant/Reviews.vue'), meta: { title: '评价管理' } },
      { path: 'ai-report', name: 'MerchantAiReport', component: () => import('@/views/merchant/AiReport.vue'), meta: { title: 'AI 经营助手' } }
    ]
  },

  // ============================ 骑手端 ============================
  {
    path: '/rider',
    component: () => import('@/layouts/RiderLayout.vue'),
    redirect: '/rider/hall',
    meta: { role: ROLE.RIDER },
    children: [
      { path: 'hall', name: 'RiderHall', component: () => import('@/views/rider/Hall.vue'), meta: { title: '抢单大厅' } },
      { path: 'delivering', name: 'RiderDelivering', component: () => import('@/views/rider/Delivering.vue'), meta: { title: '配送中' } },
      { path: 'history', name: 'RiderHistory', component: () => import('@/views/rider/History.vue'), meta: { title: '历史订单' } },
      { path: 'income', name: 'RiderIncome', component: () => import('@/views/rider/Income.vue'), meta: { title: '我的收入' } },
      { path: 'profile', name: 'RiderProfile', component: () => import('@/views/rider/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },

  // ============================ 管理员端 ============================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { role: ROLE.ADMIN },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'merchants', name: 'AdminMerchants', component: () => import('@/views/admin/Merchants.vue'), meta: { title: '商户审核' } },
      { path: 'riders', name: 'AdminRiders', component: () => import('@/views/admin/Riders.vue'), meta: { title: '骑手审核' } },
      { path: 'knowledge', name: 'AdminKnowledge', component: () => import('@/views/admin/Knowledge.vue'), meta: { title: '知识库' } },
      { path: 'tickets', name: 'AdminTickets', component: () => import('@/views/admin/Tickets.vue'), meta: { title: '客服工单' } },
      { path: 'config', name: 'AdminConfig', component: () => import('@/views/admin/Config.vue'), meta: { title: '系统配置' } }
    ]
  },

  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const requiredRole = to.meta.role as string | undefined

  document.title = to.meta.title ? `${to.meta.title} · 智能 AI 校园外卖平台` : '智能 AI 校园外卖平台'

  if (!requiredRole) {
    return true
  }
  if (!userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (requiredRole !== userStore.role) {
    // 角色不匹配：跳回自己角色的首页，避免越权访问
    return ROLE_HOME[userStore.role] || '/login'
  }
  return true
})

export default router
