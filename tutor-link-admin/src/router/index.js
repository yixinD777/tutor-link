import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/DashboardView.vue'), meta: { title: '数据概览' } },
      { path: 'users', name: 'Users', component: () => import('../views/user/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'tutors', name: 'Tutors', component: () => import('../views/tutor/TutorList.vue'), meta: { title: '家教管理' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/order/OrderList.vue'), meta: { title: '订单管理' } },
      { path: 'payments', name: 'Payments', component: () => import('../views/payment/PaymentList.vue'), meta: { title: '支付管理' } },
      { path: 'refunds', name: 'Refunds', component: () => import('../views/payment/RefundList.vue'), meta: { title: '退款管理' } },
      { path: 'certifications', name: 'Certifications', component: () => import('../views/certification/CertificationList.vue'), meta: { title: '认证审核' } },
      { path: 'reviews', name: 'Reviews', component: () => import('../views/review/ReviewList.vue'), meta: { title: '评价管理' } },
      { path: 'analytics', name: 'Analytics', component: () => import('../views/analytics/AnalyticsView.vue'), meta: { title: '数据分析' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  const token = localStorage.getItem('accessToken')
  if (!token) {
    next('/login')
    return
  }
  next()
})

export default router
