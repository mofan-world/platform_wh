<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Collection,
  Expand,
  Fold,
  FolderOpened,
  Grid,
  Key,
  Menu as MenuIcon,
  OfficeBuilding,
  Plus,
  Setting,
  SwitchButton,
  Tickets,
  User,
  UserFilled,
  ArrowDown,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { http } from '@/api/http'
import { setAppLocale, useAppI18n } from '@/i18n'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { locale, t } = useAppI18n()

interface WorkspaceTab {
  path: string
  titleKey: string
  closable: boolean
}

interface NavigationMenu {
  id: number
  parentId?: number | null
  name: string
  path?: string | null
  icon?: string | null
  permissionCode?: string | null
  sortOrder: number
  children: NavigationMenu[]
}

const tabs = ref<WorkspaceTab[]>([])
const activeTab = ref(route.fullPath)
const sidebarCollapsed = ref(localStorage.getItem('platform-identity-sidebar-collapsed') === 'true')
const sidebarMenus = ref<NavigationMenu[]>([])
const userRoles = computed(() => auth.user?.roles?.join(' / ') || t('platform.userRoles'))
const canManageUsers = computed(() => auth.hasPermission('user:manage'))
const canManageIdentity = computed(() => auth.hasPermission('identity:manage') || auth.user?.roles.includes('ADMIN'))
const systemManagementHome = computed(() => canManageIdentity.value ? '/admin/identity/organizations' : '/admin/users')
const activeMenu = computed(() => {
  if (route.path === '/admin/identity') return '/admin/identity/organizations'
  return route.path
})

const menuIconMap = {
  Collection,
  FolderOpened,
  Grid,
  Key,
  Menu: MenuIcon,
  OfficeBuilding,
  Plus,
  Setting,
  Tickets,
  User,
  UserFilled,
}

watch(
  () => route.fullPath,
  (path) => {
    activeTab.value = path
    if (tabs.value.some((tab) => tab.path === path)) return
    tabs.value.push({
      path,
      titleKey: route.meta.titleKey || 'app.workspace',
      closable: path !== systemManagementHome.value,
    })
  },
  { immediate: true },
)

function changeTab(path: string | number) {
  if (String(path) !== route.fullPath) router.push(String(path))
}

function removeTab(path: string | number) {
  const targetPath = String(path)
  const index = tabs.value.findIndex((tab) => tab.path === targetPath)
  if (index < 0 || !tabs.value[index].closable) return
  tabs.value.splice(index, 1)
  if (targetPath !== route.fullPath) return
  const nextTab = tabs.value[Math.min(index, tabs.value.length - 1)]
  router.push(nextTab?.path || systemManagementHome.value)
}

async function logout() {
  await auth.logout()
  await router.replace('/login')
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('platform-identity-sidebar-collapsed', String(sidebarCollapsed.value))
}

function changeLanguage(command: string | number | object) {
  const nextLocale = command === 'en' ? 'en' : 'zh-CN'
  setAppLocale(nextLocale)
}

function iconFor(name?: string | null) {
  return menuIconMap[name as keyof typeof menuIconMap] || Setting
}

async function loadSidebarMenus() {
  const { data } = await http.get<NavigationMenu[]>('/api/navigation/menus', {
    params: { module: 'IDENTITY' },
  })
  sidebarMenus.value = data
}

watch(
  () => `${auth.user?.roles.join('|') || ''}:${auth.user?.permissions.join('|') || ''}`,
  () => {
    if (auth.authenticated) loadSidebarMenus().catch(() => { sidebarMenus.value = [] })
    else sidebarMenus.value = []
  },
  { immediate: true },
)
</script>

<template>
  <div class="app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <header class="platform-topbar">
      <div class="platform-brand">
        <span class="platform-logo">UP</span>
        <strong>{{ t('platform.title') }}</strong>
      </div>
      <nav class="system-switcher" :aria-label="t('platform.switchSystem')">
        <a href="/tickets">{{ t('platform.issue') }}</a>
        <router-link class="active" :to="systemManagementHome">{{ t('platform.identity') }}</router-link>
      </nav>
      <div class="platform-user">
        <el-dropdown class="language-dropdown topbar-language" trigger="click" @command="changeLanguage">
          <el-button text class="language-toggle">
            {{ locale === 'en' ? 'English' : '中文' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh-CN" :disabled="locale === 'zh-CN'">中文</el-dropdown-item>
              <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <span class="online-dot"></span>
        <el-avatar :size="30">{{ auth.user?.displayName?.slice(0, 1) }}</el-avatar>
        <div class="platform-user-info">
          <strong>{{ auth.user?.displayName }}</strong>
          <span>{{ userRoles }}</span>
        </div>
        <el-button :icon="SwitchButton" circle text :title="t('app.logout')" @click="logout" />
      </div>
    </header>

    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">SM</div>
        <div>
          <strong>{{ t('platform.identity') }}</strong>
          <span>{{ t('platform.currentSystem') }}</span>
        </div>
      </div>

      <el-menu :default-active="activeMenu" :collapse="sidebarCollapsed" router>
        <template v-for="item in sidebarMenus" :key="item.id">
          <el-sub-menu v-if="item.children.length" :index="item.path || `menu-${item.id}`">
            <template #title>
              <el-icon><component :is="iconFor(item.icon)" /></el-icon>
              <span>{{ item.name }}</span>
            </template>
            <template v-for="child in item.children" :key="child.id">
              <el-sub-menu v-if="child.children.length" :index="child.path || `menu-${child.id}`">
                <template #title>
                  <el-icon><component :is="iconFor(child.icon)" /></el-icon>
                  <span>{{ child.name }}</span>
                </template>
                <el-menu-item
                  v-for="grandchild in child.children"
                  :key="grandchild.id"
                  :index="grandchild.path || `menu-${grandchild.id}`"
                  :disabled="!grandchild.path"
                >
                  <el-icon><component :is="iconFor(grandchild.icon)" /></el-icon>
                  <span>{{ grandchild.name }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item
                v-else
                :index="child.path || `menu-${child.id}`"
                :disabled="!child.path"
              >
                <el-icon><component :is="iconFor(child.icon)" /></el-icon>
                <span>{{ child.name }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>
          <el-menu-item
            v-else
            :index="item.path || `menu-${item.id}`"
            :disabled="!item.path"
          >
            <el-icon><component :is="iconFor(item.icon)" /></el-icon>
            <span>{{ item.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
      <div class="sidebar-footer">
        <el-button
          text
          class="sidebar-toggle"
          :icon="sidebarCollapsed ? Expand : Fold"
          :title="sidebarCollapsed ? t('platform.expandSidebar') : t('platform.collapseSidebar')"
          @click="toggleSidebar"
        >
          <span>{{ sidebarCollapsed ? t('platform.expandSidebar') : t('platform.collapseSidebar') }}</span>
        </el-button>
      </div>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <span class="eyebrow">SYSTEM</span>
          <h1>{{ route.meta.titleKey ? t(route.meta.titleKey) : t('platform.identity') }}</h1>
        </div>
      </header>
      <el-tabs
        v-model="activeTab"
        class="workspace-tabs"
        type="card"
        @tab-change="changeTab"
        @tab-remove="removeTab"
      >
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.path"
          :name="tab.path"
          :label="t(tab.titleKey)"
          :closable="tab.closable"
        />
      </el-tabs>
      <div class="page-body">
        <router-view />
      </div>
    </main>
  </div>
</template>
