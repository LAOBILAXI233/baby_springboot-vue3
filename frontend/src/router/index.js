import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/health',
    name: 'Health',
    component: () => import('../views/HealthView.vue'),
    meta: { title: '健康' }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/CommunityView.vue'),
    meta: { title: '社区' }
  },
  {
    path: '/my',
    name: 'My',
    component: () => import('../views/MyView.vue'),
    meta: { title: '我的' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    meta: { title: '管理后台', adminOnly: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta?.adminOnly) {
    const userStore = useUserStore()
    if (!userStore.isAdmin()) {
      next('/')
      return
    }
  }
  next()
})

export default router
