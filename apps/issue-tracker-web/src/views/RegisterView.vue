<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/http'
import { setAppLocale, useAppI18n } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const { locale, t } = useAppI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  displayName: '',
  email: '',
  password: '',
  confirmPassword: '',
})
const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: t('user.usernameRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,50}$/, message: t('user.usernamePattern'), trigger: 'blur' },
  ],
  displayName: [{ required: true, message: t('user.displayNameRequired'), trigger: 'blur' }],
  email: [
    { required: true, message: t('user.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('user.emailInvalid'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('auth.passwordRequired'), trigger: 'blur' },
    { min: 8, message: t('auth.passwordMin'), trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: t('auth.passwordPattern'), trigger: 'blur' },
  ],
  confirmPassword: [{
    validator: (_rule, value, callback) => {
      if (!value) callback(new Error(t('auth.confirmPasswordRequired')))
      else if (value !== form.password) callback(new Error(t('auth.passwordMismatch')))
      else callback()
    },
    trigger: 'blur',
  }],
}))

function changeLanguage(command: string | number | object) {
  const nextLocale = command === 'en' ? 'en' : 'zh-CN'
  setAppLocale(nextLocale)
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.register({
      username: form.username,
      displayName: form.displayName,
      email: form.email,
      password: form.password,
    })
    ElMessage.success(t('auth.registerSuccess'))
    await router.replace('/')
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
    <section class="auth-hero register-hero">
      <div class="auth-hero-content">
        <span class="eyebrow light">{{ t('auth.registerEyebrow') }}</span>
        <h1>{{ t('auth.registerHeroTitle').split('\n')[0] }}<br />{{ t('auth.registerHeroTitle').split('\n')[1] }}</h1>
        <p>{{ t('auth.registerHeroIntro') }}</p>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card wide">
        <span class="eyebrow">{{ t('auth.createAccount') }}</span>
        <h2>{{ t('auth.registerTitle') }}</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item :label="t('user.username')" prop="username">
              <el-input v-model="form.username" size="large" />
            </el-form-item>
            <el-form-item :label="t('user.displayName')" prop="displayName">
              <el-input v-model="form.displayName" size="large" />
            </el-form-item>
          </div>
          <el-form-item :label="t('user.email')" prop="email">
            <el-input v-model="form.email" size="large" />
          </el-form-item>
          <div class="form-grid">
            <el-form-item :label="t('auth.password')" prop="password">
              <el-input v-model="form.password" size="large" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('auth.confirmPassword')" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" size="large" type="password" show-password />
            </el-form-item>
          </div>
          <el-button class="full-button" type="primary" size="large" :loading="loading" @click="submit">
            {{ t('auth.createAccount') }}
          </el-button>
        </el-form>
        <p class="auth-switch">{{ t('auth.alreadyAccount') }}<router-link to="/login">{{ t('auth.backToLogin') }}</router-link></p>
      </div>
    </section>
  </div>
</template>

