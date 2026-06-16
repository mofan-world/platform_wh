<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const roles = computed(() => auth.user?.roles ?? [])

async function logout() {
  await auth.logout()
  await router.replace('/login')
}
</script>

<template>
  <section class="panel no-access-panel">
    <div class="no-access-card">
      <span class="eyebrow">ACCESS CONTROL</span>
      <h2>当前账号暂无可访问的问题跟踪功能</h2>
      <p>
        账号已登录成功，但还没有分配问题单查看、创建或管理权限。
        请联系系统管理员为该用户分配 USER、AGENT、DEVELOPER、MANAGER 或 ADMIN 等问题跟踪角色。
      </p>

      <div class="no-access-account">
        <strong>{{ auth.user?.displayName || auth.user?.username }}</strong>
        <span>{{ auth.user?.username }}</span>
        <div class="no-access-roles">
          <el-tag v-for="role in roles" :key="role" effect="plain">{{ role }}</el-tag>
          <el-tag v-if="roles.length === 0" type="warning" effect="plain">未分配角色</el-tag>
        </div>
      </div>

      <div class="no-access-actions">
        <el-button type="primary" :icon="SwitchButton" @click="logout">退出登录</el-button>
      </div>
    </div>
  </section>
</template>
