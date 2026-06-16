import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    permission?: string
    titleKey?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/identity',
    redirect: '/admin/identity',
  },
  {
    path: '/identity/',
    redirect: '/admin/identity',
  },
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, titleKey: 'nav.login' },
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true, titleKey: 'nav.register' },
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    redirect: '/admin/identity',
    children: [
      {
        path: 'admin/identity',
        component: () => import('@/views/IdentityManagementView.vue'),
        meta: { titleKey: 'nav.identityConfig', permission: 'identity:manage' },
      },
      {
        path: 'admin/users',
        component: () => import('@/views/UserManagementView.vue'),
        meta: { titleKey: 'nav.users', permission: 'user:manage' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/identity' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function hasRole(auth: ReturnType<typeof useAuthStore>, role: string) {
  return auth.user?.roles.includes(role) ?? false
}

function canAccessPermission(auth: ReturnType<typeof useAuthStore>, permission: string) {
  if (permission === 'identity:manage' && hasRole(auth, 'ADMIN')) return true
  return auth.hasPermission(permission)
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return auth.authenticated && (to.path === '/login' || to.path === '/register') ? '/admin/identity' : true
  }
  if (!auth.authenticated) return { path: '/login', query: { redirect: to.fullPath } }
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
  const permission = to.meta.permission
  if (permission && !canAccessPermission(auth, permission)) {
    window.location.assign('/tickets')
    return false
  }
  return true
})

export default router
