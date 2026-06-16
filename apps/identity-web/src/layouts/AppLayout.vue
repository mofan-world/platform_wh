<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Expand, Fold, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
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

const tabs = ref<WorkspaceTab[]>([])
const activeTab = ref(route.fullPath)
const sidebarCollapsed = ref(localStorage.getItem('platform-identity-sidebar-collapsed') === 'true')
const userRoles = computed(() => auth.user?.roles?.join(' / ') || t('platform.userRoles'))
const canManageUsers = computed(() => auth.hasPermission('user:manage'))
const canManageIdentity = computed(() => auth.hasPermission('identity:manage') || auth.user?.roles.includes('ADMIN'))
const systemManagementHome = computed(() => canManageIdentity.value ? '/admin/identity' : '/admin/users')
const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/identity')) return '/admin/identity'
  return '/admin/users'
})

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
        <el-menu-item v-if="canManageIdentity" index="/admin/identity">
          <el-icon><Setting /></el-icon>
          <span>{{ t('nav.identityConfig') }}</span>
        </el-menu-item>
        <el-menu-item v-if="canManageUsers" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>{{ t('nav.users') }}</span>
        </el-menu-item>
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
