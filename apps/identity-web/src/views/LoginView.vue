<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/http'
import { setAppLocale, useAppI18n } from '@/i18n'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { locale, t } = useAppI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('auth.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordRequired'), trigger: 'blur' }],
}))

function changeLanguage(command: string | number | object) {
  const nextLocale = command === 'en' ? 'en' : 'zh-CN'
  setAppLocale(nextLocale)
}

async function navigateAfterAuth(redirect: string) {
  const normalized = redirect === '/identity' || redirect === '/identity/' || redirect === '/admin/identity'
    ? '/admin/identity/organizations'
    : redirect
  if (normalized.startsWith('/admin/')) {
    await router.replace(normalized)
    return
  }
  window.location.assign(normalized || '/tickets')
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success(t('auth.loginSuccess'))
    const redirect = (route.query.redirect as string) || '/'
    await navigateAfterAuth(redirect)
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-dropdown class="language-switcher language-dropdown" trigger="click" @command="changeLanguage">
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
    <section class="auth-hero">
      <div class="auth-hero-content">
        <span class="eyebrow light">{{ t('auth.eyebrow') }}</span>
        <h1>{{ t('auth.heroTitle').split('\n')[0] }}<br />{{ t('auth.heroTitle').split('\n')[1] }}</h1>
        <p>{{ t('auth.heroIntro') }}</p>
        <div class="hero-metrics">
          <div><strong>SSO</strong><span>{{ t('auth.sso') }}</span></div>
          <div><strong>RBAC</strong><span>{{ t('auth.rbac') }}</span></div>
          <div><strong>Nacos</strong><span>{{ t('auth.microservice') }}</span></div>
        </div>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <span class="eyebrow">{{ t('auth.welcome') }}</span>
        <h2>{{ t('auth.loginTitle') }}</h2>
        <p class="muted">{{ t('auth.loginSubtitle') }}</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item :label="t('auth.username')" prop="username">
            <el-input v-model="form.username" size="large" :placeholder="t('auth.usernamePlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('auth.password')" prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password :placeholder="t('auth.passwordPlaceholder')" />
          </el-form-item>
          <el-button class="full-button" type="primary" size="large" :loading="loading" @click="submit">
            {{ t('auth.loginButton') }}
          </el-button>
        </el-form>
        <p class="auth-switch">{{ t('auth.noAccount') }}<router-link to="/register">{{ t('auth.registerNow') }}</router-link></p>
      </div>
    </section>
  </div>
</template>

