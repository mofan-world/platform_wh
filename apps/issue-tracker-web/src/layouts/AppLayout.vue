<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
<<<<<<< HEAD
import { Tickets, Plus, User, SwitchButton, Collection, FolderOpened, Fold, Expand, ArrowDown, Setting } from '@element-plus/icons-vue'
=======
import { Tickets, Plus, SwitchButton, Collection, FolderOpened, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import { setAppLocale, useAppI18n } from '@/i18n'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const projects = useProjectStore()
const { locale, t } = useAppI18n()

interface WorkspaceTab {
  path: string
  titleKey: string
  closable: boolean
}

const tabs = ref<WorkspaceTab[]>([])
const activeTab = ref(route.fullPath)
const sidebarCollapsed = ref(localStorage.getItem('platform-sidebar-collapsed') === 'true')

<<<<<<< HEAD
const activeSystem = computed(() =>
  route.path.startsWith('/admin/users') || route.path.startsWith('/admin/identity') ? 'identity' : 'issue',
)
const activeSystemTitle = computed(() => activeSystem.value === 'identity' ? t('platform.identity') : t('platform.issue'))
=======
const activeSystemTitle = computed(() => t('platform.issue'))
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
const userRoles = computed(() => auth.user?.roles?.join(' / ') || t('platform.userRoles'))
const canReadTickets = computed(() => auth.hasPermission('ticket:read:own') || auth.hasPermission('ticket:read:all'))
const canCreateTicket = computed(() => auth.hasPermission('ticket:create'))
const canManageVersions = computed(() => auth.hasPermission('version:manage'))
const canManageProjects = computed(() => auth.hasPermission('project:manage'))
const canManageUsers = computed(() => auth.hasPermission('user:manage'))
const canManageIdentity = computed(() => auth.hasPermission('identity:manage'))

const activeMenu = computed(() => {
  if (route.path === '/no-access') return '/no-access'
  if (route.path.startsWith('/admin/identity')) return '/admin/identity'
  if (route.path.startsWith('/admin/projects')) return '/admin/projects'
  if (route.path.startsWith('/admin/versions')) return '/admin/versions'
  if (route.path === '/tickets/new') return '/tickets/new'
  return '/tickets'
})

watch(
  () => route.fullPath,
  (path) => {
    activeTab.value = path
    if (tabs.value.some((tab) => tab.path === path)) return
    tabs.value.push({
      path,
      titleKey: route.meta.titleKey || 'app.workspace',
      closable: !['/tickets', '/no-access'].includes(path),
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
  router.push(nextTab?.path || (canReadTickets.value ? '/tickets' : '/no-access'))
}

async function logout() {
  await auth.logout()
  projects.reset()
  window.location.assign('/login')
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('platform-sidebar-collapsed', String(sidebarCollapsed.value))
}

function changeLanguage(command: string | number | object) {
  const nextLocale = command === 'en' ? 'en' : 'zh-CN'
  setAppLocale(nextLocale)
}

async function changeProject(projectId: number) {
  projects.setCurrentProject(projectId)
  if (route.path !== '/tickets') await router.push('/tickets')
}

watch(
  canReadTickets,
  (canRead) => {
    if (canRead) projects.loadProjects()
    else projects.reset()
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
<<<<<<< HEAD
        <router-link v-if="canReadTickets" :class="{ active: activeSystem === 'issue' }" to="/tickets">{{ t('platform.issue') }}</router-link>
        <router-link
          v-if="canManageUsers || canManageIdentity"
          :class="{ active: activeSystem === 'identity' }"
          :to="canManageUsers ? '/admin/users' : '/admin/identity'"
=======
        <router-link class="active" to="/tickets">{{ t('platform.issue') }}</router-link>
        <a href="/travel/">{{ t('platform.travel') }}</a>
        <a
          v-if="auth.hasPermission('user:manage')"
          href="/identity/"
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
        >
          {{ t('platform.identity') }}
        </a>
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
        <div class="brand-mark">IT</div>
        <div>
          <strong>{{ activeSystemTitle }}</strong>
          <span>{{ t('platform.currentSystem') }}</span>
        </div>
      </div>

      <el-menu :default-active="activeMenu" :collapse="sidebarCollapsed" router>
<<<<<<< HEAD
        <template v-if="activeSystem === 'issue'">
        <el-menu-item v-if="canReadTickets" index="/tickets">
=======
        <el-menu-item index="/tickets">
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
          <el-icon><Tickets /></el-icon>
          <span>{{ t('nav.tickets') }}</span>
        </el-menu-item>
        <el-menu-item v-if="canCreateTicket" index="/tickets/new">
          <el-icon><Plus /></el-icon>
          <span>{{ t('nav.createTicket') }}</span>
        </el-menu-item>
        <el-menu-item v-if="canManageVersions" index="/admin/versions">
          <el-icon><Collection /></el-icon>
          <span>{{ t('nav.versions') }}</span>
        </el-menu-item>
        <el-menu-item v-if="canManageProjects" index="/admin/projects">
          <el-icon><FolderOpened /></el-icon>
          <span>{{ t('nav.projects') }}</span>
        </el-menu-item>
<<<<<<< HEAD
        </template>
        <template v-else>
        <el-menu-item v-if="canManageUsers" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>{{ t('nav.users') }}</span>
        </el-menu-item>
        <el-menu-item v-if="canManageIdentity" index="/admin/identity">
          <el-icon><Setting /></el-icon>
          <span>{{ t('nav.identityConfig') }}</span>
        </el-menu-item>
        </template>
=======
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
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
          <span class="eyebrow">WORKSPACE</span>
          <h1>{{ route.meta.titleKey ? t(route.meta.titleKey) : t('app.workspace') }}</h1>
        </div>
        <div class="topbar-actions">
          <el-select
<<<<<<< HEAD
            v-if="activeSystem === 'issue' && canReadTickets"
=======
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd
            v-model="projects.currentProjectId"
            class="project-switcher"
            :loading="projects.loading"
            :placeholder="t('project.selectCurrent')"
            @change="changeProject"
          >
            <el-option
              v-for="project in projects.projects"
              :key="project.id"
              :label="`${project.name} (${project.code})`"
              :value="project.id"
            />
          </el-select>
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
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>
      </div>
    </main>
  </div>
</template>
