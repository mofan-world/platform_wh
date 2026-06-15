<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/http'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    if (redirect.startsWith('/travel/')) {
      window.location.assign(redirect)
      return
    }
    await router.replace(redirect)
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-hero">
      <div class="auth-hero-content">
        <span class="eyebrow light">PLATFORM IDENTITY</span>
        <h1>统一身份认证<br />连接所有业务系统</h1>
        <p>一次登录后可访问问题跟踪、出差车票和后续接入的业务系统，角色权限由身份认证中心统一维护。</p>
        <div class="hero-metrics">
          <div><strong>SSO</strong><span>统一登录</span></div>
          <div><strong>RBAC</strong><span>角色授权</span></div>
          <div><strong>Nacos</strong><span>微服务接入</span></div>
        </div>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <span class="eyebrow">WELCOME BACK</span>
        <h2>登录统一平台</h2>
        <p class="muted">使用统一身份认证账号继续</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button class="full-button" type="primary" size="large" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>
        <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </section>
  </div>
</template>

