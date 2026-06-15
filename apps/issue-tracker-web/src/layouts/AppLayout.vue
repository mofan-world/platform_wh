<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Tickets, Plus, User, SwitchButton, Collection, FolderOpened, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
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

const activeSystem = computed(() => route.path.startsWith('/admin/users') ? 'identity' : 'issue')
const activeSystemTitle = computed(() => activeSystem.value === 'identity' ? t('platform.identity') : t('platform.issue'))
const userRoles = computed(() => auth.user?.roles?.join(' / ') || t('platform.userRoles'))

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/projects')) return '/admin/projects'
  if (route.path.startsWith('/admin/versions')) return '/admin/versions'
  if (route.path.startsWith('/admin')) return '/admin/users'
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
      closable: path !== '/tickets',
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
  router.push(nextTab?.path || '/tickets')
}

async function logout() {
  await auth.logout()
  projects.reset()
  await router.replace('/login')
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

onMounted(() => projects.loadProjects())
</script>

<template>
  <div class="app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <header class="platform-topbar">
      <div class="platform-brand">
        <span class="platform-logo">UP</span>
        <strong>{{ t('platform.title') }}</strong>
      </div>
      <nav class="system-switcher" :aria-label="t('platform.switchSystem')">
        <router-link :class="{ active: activeSystem === 'issue' }" to="/tickets">{{ t('platform.issue') }}</router-link>
        <a href="/travel/">{{ t('platform.travel') }}</a>
        <router-link
          v-if="auth.hasPermission('user:manage')"
          :class="{ active: activeSystem === 'identity' }"
          to="/admin/users"
        >
          {{ t('platform.identity') }}
        </router-link>
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
        <template v-if="activeSystem === 'issue'">
        <el-menu-item index="/tickets">
          <el-icon><Tickets /></el-icon>
          <span>{{ t('nav.tickets') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasPermission('ticket:create')" index="/tickets/new">
          <el-icon><Plus /></el-icon>
          <span>{{ t('nav.createTicket') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasPermission('version:manage')" index="/admin/versions">
          <el-icon><Collection /></el-icon>
          <span>{{ t('nav.versions') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasPermission('project:manage')" index="/admin/projects">
          <el-icon><FolderOpened /></el-icon>
          <span>{{ t('nav.projects') }}</span>
        </el-menu-item>
        </template>
        <template v-else>
        <el-menu-item v-if="auth.hasPermission('user:manage')" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>{{ t('nav.users') }}</span>
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
          <span class="eyebrow">WORKSPACE</span>
          <h1>{{ route.meta.titleKey ? t(route.meta.titleKey) : t('app.workspace') }}</h1>
        </div>
        <div class="topbar-actions">
          <el-select
            v-if="activeSystem === 'issue'"
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
