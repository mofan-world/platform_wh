import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    permission?: string
    titleKey?: string
    identitySection?: string
  }
}

const identityManagementRoutes: RouteRecordRaw[] = [
  {
    path: 'admin/identity',
    redirect: '/admin/identity/organizations',
  },
  {
    path: 'admin/identity/organizations',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.organizations', permission: 'identity:manage', identitySection: 'organizations' },
  },
  {
    path: 'admin/identity/menus',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.menus', permission: 'identity:manage', identitySection: 'menus' },
  },
  {
    path: 'admin/identity/permissions',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.permissions', permission: 'identity:manage', identitySection: 'permissions' },
  },
  {
    path: 'admin/identity/roles-posts',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.rolesPosts', permission: 'identity:manage', identitySection: 'roles-posts' },
  },
  {
    path: 'admin/identity/modules',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.modules', permission: 'identity:manage', identitySection: 'modules' },
  },
  {
    path: 'admin/identity/dictionaries',
    component: () => import('@/views/IdentityManagementView.vue'),
    meta: { titleKey: 'nav.dictionaries', permission: 'identity:manage', identitySection: 'dictionaries' },
  },
]

const routes: RouteRecordRaw[] = [
  {
    path: '/identity',
    redirect: '/admin/identity/organizations',
  },
  {
    path: '/identity/',
    redirect: '/admin/identity/organizations',
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
    redirect: '/admin/identity/organizations',
    children: [
      ...identityManagementRoutes,
      {
        path: 'admin/users',
        component: () => import('@/views/UserManagementView.vue'),
        meta: { titleKey: 'nav.users', permission: 'user:manage' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/identity/organizations' },
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
    return auth.authenticated && (to.path === '/login' || to.path === '/register') ? '/admin/identity/organizations' : true
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
