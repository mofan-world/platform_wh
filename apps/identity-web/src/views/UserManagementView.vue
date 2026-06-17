<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { http, errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { PageResult } from '@/types'
import { useAppI18n } from '@/i18n'

interface Role {
  id: number
  code: string
  name: string
  permissions: string[]
}

interface OrganizationOption {
  id: number
  code: string
  name: string
}

interface PostOption {
  id: number
  code: string
  name: string
}

interface User {
  id: number
  username: string
  email: string
  displayName: string
  enabled: boolean
  roles: string[]
  organizationId?: number | null
  organizationName?: string | null
  postId?: number | null
  postName?: string | null
  createdAt: string
}

const auth = useAuthStore()
const { t } = useAppI18n()
const loading = ref(false)
const saving = ref(false)
const users = ref<User[]>([])
const roles = ref<Role[]>([])
const organizations = ref<OrganizationOption[]>([])
const posts = ref<PostOption[]>([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 20 })
const dialogVisible = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  email: '',
  displayName: '',
  password: '',
  enabled: true,
  organizationId: undefined as number | undefined,
  postId: undefined as number | undefined,
  roleIds: [] as number[],
})
const dialogTitle = computed(() => editingId.value ? t('user.edit') : t('user.add'))
const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: t('user.usernameRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,50}$/, message: t('user.usernamePattern'), trigger: 'blur' },
  ],
  email: [
    { required: true, message: t('user.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('user.emailInvalid'), trigger: 'blur' },
  ],
  displayName: [{ required: true, message: t('user.displayNameRequired'), trigger: 'blur' }],
  organizationId: [{ required: true, message: t('user.organizationRequired'), trigger: 'change' }],
  postId: [{ required: true, message: t('user.postRequired'), trigger: 'change' }],
  password: [{
    validator: (_rule, value, callback) => {
      if (!editingId.value && !value) {
        callback(new Error(t('user.passwordRequiredOnCreate')))
      } else if (value && (value.length < 8 || !/[A-Za-z]/.test(value) || !/\d/.test(value))) {
        callback(new Error(t('user.passwordPattern')))
      } else {
        callback()
      }
    },
    trigger: 'blur',
  }],
  roleIds: [{ type: 'array', min: 1, required: true, message: t('user.roleRequired'), trigger: 'change' }],
}))

function format(path: string, vars: Record<string, string | number>) {
  return Object.entries(vars).reduce(
    (message, [key, value]) => message.replace(`{${key}}`, String(value)),
    t(path),
  )
}

async function load() {
  loading.value = true
  try {
    const [usersResult, rolesResult, organizationsResult, postsResult] = await Promise.allSettled([
      http.get<PageResult<User>>('/api/admin/users', {
        params: { keyword: query.keyword || undefined, page: query.page - 1, size: query.size },
      }),
      http.get<Role[]>('/api/admin/roles'),
      http.get<OrganizationOption[]>('/api/admin/users/options/organizations'),
      http.get<PostOption[]>('/api/admin/users/options/posts'),
    ])
    if (usersResult.status === 'fulfilled') {
      users.value = usersResult.value.data.content
      total.value = usersResult.value.data.totalElements
    } else {
      users.value = []
      total.value = 0
      ElMessage.error(errorMessage(usersResult.reason))
    }
    if (rolesResult.status === 'fulfilled') {
      roles.value = rolesResult.value.data
    } else {
      roles.value = []
      ElMessage.error(format('user.loadRolesFailed', { message: errorMessage(rolesResult.reason) }))
    }
    if (organizationsResult.status === 'fulfilled') {
      organizations.value = organizationsResult.value.data
    } else {
      organizations.value = []
      ElMessage.error(errorMessage(organizationsResult.reason))
    }
    if (postsResult.status === 'fulfilled') {
      posts.value = postsResult.value.data
    } else {
      posts.value = []
      ElMessage.error(errorMessage(postsResult.reason))
    }
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    username: '',
    email: '',
    displayName: '',
    password: '',
    enabled: true,
    organizationId: undefined,
    postId: undefined,
    roleIds: [],
  })
  formRef.value?.clearValidate()
}

function createUser() {
  resetForm()
  form.organizationId = organizations.value[0]?.id
  form.postId = posts.value[0]?.id
  dialogVisible.value = true
}

function editUser(user: User) {
  editingId.value = user.id
  Object.assign(form, {
    username: user.username,
    email: user.email,
    displayName: user.displayName,
    password: '',
    enabled: user.enabled,
    organizationId: user.organizationId ?? undefined,
    postId: user.postId ?? undefined,
    roleIds: roles.value.filter((role) => user.roles.includes(role.code)).map((role) => role.id),
  })
  dialogVisible.value = true
}

async function saveUser() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, password: form.password || null }
    if (editingId.value) {
      await http.put(`/api/admin/users/${editingId.value}`, payload)
    } else {
      await http.post('/api/admin/users', payload)
    }
    ElMessage.success(editingId.value ? t('user.updated') : t('user.created'))
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(user: User) {
  try {
    await http.patch(`/api/admin/users/${user.id}/enabled`, { enabled: user.enabled })
    ElMessage.success(user.enabled ? t('user.enabledMessage') : t('user.disabledMessage'))
  } catch (error) {
    user.enabled = !user.enabled
    ElMessage.error(errorMessage(error))
  }
}

async function deleteUser(user: User) {
  try {
    await ElMessageBox.confirm(
      format('user.deleteConfirm', { name: user.displayName, username: user.username }),
      t('user.deleteTitle'),
      { type: 'warning', confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel') },
    )
    await http.delete(`/api/admin/users/${user.id}`)
    ElMessage.success(t('user.deleted'))
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(errorMessage(error))
  }
}

function search() {
  query.page = 1
  load()
}

onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="section-heading compact">
      <div>
        <span class="eyebrow">{{ t('user.accessControl') }}</span>
        <h2>{{ t('user.title') }}</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="createUser">{{ t('user.add') }}</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        class="search-input"
        clearable
        :placeholder="t('user.searchPlaceholder')"
        @keyup.enter="search"
      />
      <el-button @click="search">{{ t('common.search') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="users">
      <el-table-column prop="username" :label="t('user.username')" width="145" />
      <el-table-column prop="displayName" :label="t('user.displayName')" width="145" />
      <el-table-column prop="email" :label="t('user.email')" min-width="220" />
      <el-table-column :label="t('user.organization')" min-width="150">
        <template #default="{ row }">{{ row.organizationName || t('common.noData') }}</template>
      </el-table-column>
      <el-table-column :label="t('user.post')" min-width="130">
        <template #default="{ row }">{{ row.postName || t('common.noData') }}</template>
      </el-table-column>
      <el-table-column :label="t('user.roles')" min-width="220">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" class="role-tag" effect="plain">{{ role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('user.createdAt')" width="175">
        <template #default="{ row }">{{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('user.enabled')" width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            :disabled="row.id === auth.user?.id"
            @change="toggleEnabled(row)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="145" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="editUser(row)">{{ t('common.edit') }}</el-button>
          <el-button link type="danger" :disabled="row.id === auth.user?.id" @click="deleteUser(row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @change="load"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item :label="t('user.username')" prop="username">
            <el-input v-model="form.username" autocomplete="off" />
          </el-form-item>
          <el-form-item :label="t('user.displayName')" prop="displayName">
            <el-input v-model="form.displayName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item :label="t('user.email')" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item :label="editingId ? t('user.resetPassword') : t('user.password')" prop="password">
            <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item :label="t('user.organization')" prop="organizationId">
            <el-select v-model="form.organizationId" clearable filterable class="full-width">
              <el-option
                v-for="organization in organizations"
                :key="organization.id"
                :label="`${organization.name} (${organization.code})`"
                :value="organization.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('user.post')" prop="postId">
            <el-select v-model="form.postId" clearable filterable class="full-width">
              <el-option
                v-for="post in posts"
                :key="post.id"
                :label="`${post.name} (${post.code})`"
                :value="post.id"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="t('user.roles')" prop="roleIds">
          <el-checkbox-group v-model="form.roleIds" class="role-options">
            <el-checkbox v-for="role in roles" :key="role.id" :value="role.id">
              <strong>{{ role.name }}</strong>
              <span>{{ role.code }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item :label="t('user.accountStatus')">
          <el-switch v-model="form.enabled" :active-text="t('common.enabled')" :inactive-text="t('common.disabled')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>
