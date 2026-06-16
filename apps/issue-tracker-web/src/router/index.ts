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
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    redirect: '/tickets',
    children: [
      {
        path: 'tickets',
        component: () => import('@/views/TicketListView.vue'),
        meta: { titleKey: 'nav.tickets', permission: 'ticket:read:own' },
      },
      {
        path: 'tickets/new',
        component: () => import('@/views/TicketCreateView.vue'),
        meta: { titleKey: 'nav.createTicket', permission: 'ticket:create' },
      },
      {
        path: 'tickets/:id',
        component: () => import('@/views/TicketDetailView.vue'),
        meta: { titleKey: 'nav.ticketDetail', permission: 'ticket:read:own' },
      },
      {
<<<<<<< HEAD
        path: 'admin/users',
        component: () => import('@/views/UserManagementView.vue'),
        meta: { titleKey: 'nav.users', permission: 'user:manage' },
      },
      {
        path: 'admin/identity',
        component: () => import('@/views/IdentityManagementView.vue'),
        meta: { titleKey: 'nav.identityConfig', permission: 'identity:manage' },
      },
      {
=======
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
        path: 'admin/versions',
        component: () => import('@/views/VersionManagementView.vue'),
        meta: { titleKey: 'nav.versions', permission: 'version:manage' },
      },
      {
        path: 'admin/projects',
        component: () => import('@/views/ProjectManagementView.vue'),
        meta: { titleKey: 'nav.projects', permission: 'project:manage' },
      },
      {
        path: 'no-access',
        component: () => import('@/views/NoAccessView.vue'),
        meta: { titleKey: 'nav.noAccess' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function canAccessPermission(auth: ReturnType<typeof useAuthStore>, permission: string) {
  if (permission === 'ticket:read:own') {
    return auth.hasPermission('ticket:read:own') || auth.hasPermission('ticket:read:all')
  }
  return auth.hasPermission(permission)
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.authenticated) {
    window.location.assign(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return false
  }
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      window.location.assign(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return false
    }
  }
  const permission = to.meta.permission
  if (permission && !canAccessPermission(auth, permission)) {
    return { path: '/no-access', replace: true }
  }
  return true
})

export default router
