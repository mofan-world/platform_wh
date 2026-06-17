<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { errorMessage, http } from '@/api/http'

type ResourceKind = 'organization' | 'module' | 'permission' | 'role' | 'post' | 'menu' | 'dictType' | 'dictItem'
type SectionName = 'organizations' | 'menus' | 'permissions' | 'roles-posts' | 'modules' | 'dictionaries'

interface OrganizationView {
  id: number
  parentId?: number | null
  parentName?: string | null
  code: string
  name: string
  type: string
  sortOrder: number
  leader?: string | null
  phone?: string | null
  email?: string | null
  description?: string | null
  enabled: boolean
}

interface ModuleView {
  id: number
  code: string
  name: string
  routePrefix?: string | null
  description?: string | null
  enabled: boolean
  sortOrder: number
}

interface PermissionView {
  id: number
  code: string
  name: string
  moduleId?: number | null
  moduleName?: string | null
  description?: string | null
  enabled: boolean
  sortOrder: number
}

interface RoleView {
  id: number
  code: string
  name: string
  description?: string | null
  enabled: boolean
  sortOrder: number
  permissionIds: number[]
  permissions: string[]
}

interface PostView {
  id: number
  code: string
  name: string
  sortOrder: number
  description?: string | null
  enabled: boolean
}

interface MenuView {
  id: number
  parentId?: number | null
  parentName?: string | null
  moduleId?: number | null
  moduleName?: string | null
  name: string
  path?: string | null
  component?: string | null
  icon?: string | null
  permissionCode?: string | null
  sortOrder: number
  visible: boolean
  enabled: boolean
}

interface DictionaryTypeView {
  id: number
  code: string
  name: string
  description?: string | null
  enabled: boolean
}

interface DictionaryItemView {
  id: number
  typeId: number
  typeCode: string
  label: string
  value: string
  sortOrder: number
  remark?: string | null
  enabled: boolean
}

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogKind = ref<ResourceKind>('organization')
const editingId = ref<number>()

const organizations = ref<OrganizationView[]>([])
const modules = ref<ModuleView[]>([])
const permissions = ref<PermissionView[]>([])
const roles = ref<RoleView[]>([])
const posts = ref<PostView[]>([])
const menus = ref<MenuView[]>([])
const dictionaryTypes = ref<DictionaryTypeView[]>([])
const dictionaryItems = ref<DictionaryItemView[]>([])
const selectedDictionaryTypeId = ref<number>()

const form = reactive({
  code: '',
  name: '',
  parentId: undefined as number | undefined,
  type: 'DEPARTMENT',
  sortOrder: 0,
  leader: '',
  phone: '',
  email: '',
  routePrefix: '',
  description: '',
  enabled: true,
  moduleId: undefined as number | undefined,
  permissionIds: [] as number[],
  path: '',
  component: '',
  icon: '',
  permissionCode: '',
  visible: true,
  typeId: undefined as number | undefined,
  label: '',
  value: '',
  remark: '',
})

const labels: Record<ResourceKind, string> = {
  organization: '组织机构',
  module: '微服务模块',
  permission: '权限',
  role: '角色',
  post: '岗位',
  menu: '菜单',
  dictType: '字典类型',
  dictItem: '字典项',
}

const rules: FormRules = {
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

const dialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${labels[dialogKind.value]}`)
const selectedDictionaryType = computed(() =>
  dictionaryTypes.value.find((item) => item.id === selectedDictionaryTypeId.value),
)
const availableParentMenus = computed(() =>
  menus.value.filter((menu) =>
    menu.id !== editingId.value
    && menuDepth(menu) < 3
    && !isMenuDescendant(menu.id, editingId.value),
  ),
)
const sectionTitles: Record<SectionName, string> = {
  organizations: '组织机构管理',
  menus: '菜单管理',
  permissions: '权限管理',
  'roles-posts': '角色岗位管理',
  modules: '微服务模块管理',
  dictionaries: '字典管理',
}
const activeSection = computed<SectionName>(() => {
  const value = route.meta.identitySection
  return typeof value === 'string' && sectionTitles[value as SectionName]
    ? value as SectionName
    : 'organizations'
})

function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    code: '',
    name: '',
    parentId: undefined,
    type: 'DEPARTMENT',
    sortOrder: 0,
    leader: '',
    phone: '',
    email: '',
    routePrefix: '',
    description: '',
    enabled: true,
    moduleId: undefined,
    permissionIds: [],
    path: '',
    component: '',
    icon: '',
    permissionCode: '',
    visible: true,
    typeId: selectedDictionaryTypeId.value,
    label: '',
    value: '',
    remark: '',
  })
  formRef.value?.clearValidate()
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([
      loadOrganizations(),
      loadModules(),
      loadPermissions(),
      loadRoles(),
      loadPosts(),
      loadMenus(),
      loadDictionaryTypes(),
    ])
    selectedDictionaryTypeId.value ??= dictionaryTypes.value[0]?.id
    if (selectedDictionaryTypeId.value) await loadDictionaryItems()
  } finally {
    loading.value = false
  }
}

async function loadOrganizations() {
  const { data } = await http.get<OrganizationView[]>('/api/admin/identity/organizations')
  organizations.value = data
}

async function loadModules() {
  const { data } = await http.get<ModuleView[]>('/api/admin/identity/modules')
  modules.value = data
}

async function loadPermissions() {
  const { data } = await http.get<PermissionView[]>('/api/admin/identity/permissions')
  permissions.value = data
}

async function loadRoles() {
  const { data } = await http.get<RoleView[]>('/api/admin/identity/roles')
  roles.value = data
}

async function loadPosts() {
  const { data } = await http.get<PostView[]>('/api/admin/identity/posts')
  posts.value = data
}

async function loadMenus() {
  const { data } = await http.get<MenuView[]>('/api/admin/identity/menus')
  menus.value = data
}

async function loadDictionaryTypes() {
  const { data } = await http.get<DictionaryTypeView[]>('/api/admin/identity/dictionary-types')
  dictionaryTypes.value = data
}

async function loadDictionaryItems() {
  if (!selectedDictionaryTypeId.value) {
    dictionaryItems.value = []
    return
  }
  const { data } = await http.get<DictionaryItemView[]>('/api/admin/identity/dictionary-items', {
    params: { typeId: selectedDictionaryTypeId.value },
  })
  dictionaryItems.value = data
}

function openCreate(kind: ResourceKind) {
  dialogKind.value = kind
  resetForm()
  if (kind === 'dictItem') form.typeId = selectedDictionaryTypeId.value
  dialogVisible.value = true
}

function openEdit(kind: ResourceKind, row: Record<string, unknown>) {
  dialogKind.value = kind
  resetForm()
  editingId.value = Number(row.id)
  Object.assign(form, {
    code: String(row.code ?? ''),
    name: String(row.name ?? ''),
    parentId: row.parentId as number | undefined,
    type: String(row.type ?? 'DEPARTMENT'),
    sortOrder: Number(row.sortOrder ?? 0),
    leader: String(row.leader ?? ''),
    phone: String(row.phone ?? ''),
    email: String(row.email ?? ''),
    routePrefix: String(row.routePrefix ?? ''),
    description: String(row.description ?? ''),
    enabled: Boolean(row.enabled ?? true),
    moduleId: row.moduleId as number | undefined,
    permissionIds: Array.isArray(row.permissionIds) ? row.permissionIds : [],
    path: String(row.path ?? ''),
    component: String(row.component ?? ''),
    icon: String(row.icon ?? ''),
    permissionCode: String(row.permissionCode ?? ''),
    visible: Boolean(row.visible ?? true),
    typeId: (row.typeId as number | undefined) ?? selectedDictionaryTypeId.value,
    label: String(row.label ?? ''),
    value: String(row.value ?? ''),
    remark: String(row.remark ?? ''),
  })
  dialogVisible.value = true
}

function endpoint(kind: ResourceKind, id?: number) {
  const base = {
    organization: '/api/admin/identity/organizations',
    module: '/api/admin/identity/modules',
    permission: '/api/admin/identity/permissions',
    role: '/api/admin/identity/roles',
    post: '/api/admin/identity/posts',
    menu: '/api/admin/identity/menus',
    dictType: '/api/admin/identity/dictionary-types',
    dictItem: '/api/admin/identity/dictionary-items',
  }[kind]
  return id ? `${base}/${id}` : base
}

function payload(kind: ResourceKind) {
  if (kind === 'organization') {
    return pick('parentId', 'code', 'name', 'type', 'sortOrder', 'leader', 'phone', 'email', 'description', 'enabled')
  }
  if (kind === 'module') {
    return pick('code', 'name', 'routePrefix', 'description', 'enabled', 'sortOrder')
  }
  if (kind === 'permission') {
    return pick('code', 'name', 'moduleId', 'description', 'enabled', 'sortOrder')
  }
  if (kind === 'role') {
    return pick('code', 'name', 'description', 'enabled', 'sortOrder', 'permissionIds')
  }
  if (kind === 'post') {
    return pick('code', 'name', 'sortOrder', 'description', 'enabled')
  }
  if (kind === 'menu') {
    return pick('parentId', 'moduleId', 'name', 'path', 'component', 'icon', 'permissionCode', 'sortOrder', 'visible', 'enabled')
  }
  if (kind === 'dictType') {
    return pick('code', 'name', 'description', 'enabled')
  }
  return pick('typeId', 'label', 'value', 'sortOrder', 'remark', 'enabled')
}

function pick(...keys: Array<keyof typeof form>) {
  return Object.fromEntries(keys.map((key) => [key, form[key]]))
}

async function reload(kind: ResourceKind) {
  if (kind === 'organization') await loadOrganizations()
  else if (kind === 'module') await Promise.all([loadModules(), loadPermissions(), loadMenus()])
  else if (kind === 'permission') await Promise.all([loadPermissions(), loadRoles()])
  else if (kind === 'role') await loadRoles()
  else if (kind === 'post') await loadPosts()
  else if (kind === 'menu') await loadMenus()
  else if (kind === 'dictType') {
    await loadDictionaryTypes()
    selectedDictionaryTypeId.value = dictionaryTypes.value[0]?.id
    await loadDictionaryItems()
  } else {
    await loadDictionaryItems()
  }
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await http.put(endpoint(dialogKind.value, editingId.value), payload(dialogKind.value))
    } else {
      await http.post(endpoint(dialogKind.value), payload(dialogKind.value))
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await reload(dialogKind.value)
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    saving.value = false
  }
}

async function remove(kind: ResourceKind, id: number) {
  try {
    await ElMessageBox.confirm(`确认删除该${labels[kind]}？`, '删除确认', { type: 'warning' })
    await http.delete(endpoint(kind, id))
    ElMessage.success('删除成功')
    await reload(kind)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(errorMessage(error))
  }
}

function selectDictionaryType(row?: DictionaryTypeView) {
  selectedDictionaryTypeId.value = row?.id
}

function menuDepth(menu: MenuView) {
  let depth = 1
  let parentId = menu.parentId
  while (parentId) {
    const parent = menus.value.find((item) => item.id === parentId)
    if (!parent) break
    depth += 1
    parentId = parent.parentId
  }
  return depth
}

function isMenuDescendant(candidateId: number, ancestorId?: number) {
  if (!ancestorId) return false
  let current = menus.value.find((item) => item.id === candidateId)
  while (current?.parentId) {
    if (current.parentId === ancestorId) return true
    current = menus.value.find((item) => item.id === current?.parentId)
  }
  return false
}

watch(selectedDictionaryTypeId, () => {
  loadDictionaryItems().catch((error) => ElMessage.error(errorMessage(error)))
})

onMounted(() => {
  loadAll().catch((error) => ElMessage.error(errorMessage(error)))
})
</script>

<template>
  <section class="panel identity-management">

    <div class="identity-section">
      <template v-if="activeSection === 'organizations'">
        <div class="section-heading compact sub-heading">
          <h2>组织机构管理</h2>
          <el-button type="primary" :icon="Plus" @click="openCreate('organization')">新增组织</el-button>
        </div>
        <el-table v-loading="loading" :data="organizations" row-key="id">
          <el-table-column prop="code" label="编码" width="150" />
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="parentName" label="上级组织" min-width="150" />
          <el-table-column prop="leader" label="负责人" width="120" />
          <el-table-column prop="sortOrder" label="排序" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit('organization', row)">编辑</el-button>
              <el-button link type="danger" @click="remove('organization', row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else-if="activeSection === 'menus'">
        <div class="section-heading compact sub-heading">
          <h2>菜单管理</h2>
          <el-button type="primary" :icon="Plus" @click="openCreate('menu')">新增菜单</el-button>
        </div>
        <el-table v-loading="loading" :data="menus" row-key="id">
          <el-table-column prop="name" label="名称" min-width="150" />
          <el-table-column prop="moduleName" label="所属模块" min-width="150" />
          <el-table-column prop="parentName" label="上级菜单" min-width="140" />
          <el-table-column prop="path" label="网关路由" min-width="180" show-overflow-tooltip />
          <el-table-column prop="component" label="前端组件" min-width="160" show-overflow-tooltip />
          <el-table-column prop="permissionCode" label="权限标识" min-width="170" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="可见" width="80">
            <template #default="{ row }">{{ row.visible ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit('menu', row)">编辑</el-button>
              <el-button link type="danger" @click="remove('menu', row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else-if="activeSection === 'permissions'">
        <div class="section-heading compact sub-heading">
          <h2>权限管理</h2>
          <el-button type="primary" :icon="Plus" @click="openCreate('permission')">新增权限</el-button>
        </div>
        <el-table v-loading="loading" :data="permissions" row-key="id">
          <el-table-column prop="code" label="权限编码" min-width="190" />
          <el-table-column prop="name" label="权限名称" min-width="150" />
          <el-table-column prop="moduleName" label="所属模块" min-width="150" />
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit('permission', row)">编辑</el-button>
              <el-button link type="danger" @click="remove('permission', row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else-if="activeSection === 'roles-posts'">
        <div class="identity-split">
          <div>
            <div class="section-heading compact sub-heading">
              <h2>角色管理</h2>
              <el-button type="primary" :icon="Plus" @click="openCreate('role')">新增角色</el-button>
            </div>
            <el-table v-loading="loading" :data="roles" row-key="id">
              <el-table-column prop="code" label="编码" width="130" />
              <el-table-column prop="name" label="名称" min-width="130" />
              <el-table-column label="权限" min-width="260" show-overflow-tooltip>
                <template #default="{ row }">{{ row.permissions.join(', ') }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openEdit('role', row)">编辑</el-button>
                  <el-button link type="danger" :disabled="row.code === 'ADMIN'" @click="remove('role', row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div>
            <div class="section-heading compact sub-heading">
              <h2>岗位管理</h2>
              <el-button type="primary" :icon="Plus" @click="openCreate('post')">新增岗位</el-button>
            </div>
            <el-table v-loading="loading" :data="posts" row-key="id">
              <el-table-column prop="code" label="编码" width="130" />
              <el-table-column prop="name" label="名称" min-width="140" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openEdit('post', row)">编辑</el-button>
                  <el-button link type="danger" @click="remove('post', row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>

      <template v-else-if="activeSection === 'modules'">
        <div class="section-heading compact sub-heading">
          <h2>微服务模块管理</h2>
          <el-button type="primary" :icon="Plus" @click="openCreate('module')">新增模块</el-button>
        </div>
        <el-table v-loading="loading" :data="modules" row-key="id">
          <el-table-column prop="code" label="模块编码" width="170" />
          <el-table-column prop="name" label="模块名称" min-width="160" />
          <el-table-column prop="routePrefix" label="路由前缀" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit('module', row)">编辑</el-button>
              <el-button link type="danger" @click="remove('module', row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else>
        <div class="identity-split dictionaries">
          <div>
            <div class="section-heading compact sub-heading">
              <h2>字典类型</h2>
              <el-button type="primary" :icon="Plus" @click="openCreate('dictType')">新增类型</el-button>
            </div>
            <el-table v-loading="loading" :data="dictionaryTypes" row-key="id" highlight-current-row @current-change="selectDictionaryType">
              <el-table-column prop="code" label="编码" width="150" />
              <el-table-column prop="name" label="名称" min-width="130" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click.stop="openEdit('dictType', row)">编辑</el-button>
                  <el-button link type="danger" @click.stop="remove('dictType', row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div>
            <div class="section-heading compact sub-heading">
              <h2>字典项 {{ selectedDictionaryType ? `- ${selectedDictionaryType.name}` : '' }}</h2>
              <el-button type="primary" :disabled="!selectedDictionaryTypeId" :icon="Plus" @click="openCreate('dictItem')">新增字典项</el-button>
            </div>
            <el-table v-loading="loading" :data="dictionaryItems" row-key="id">
              <el-table-column prop="label" label="标签" min-width="130" />
              <el-table-column prop="value" label="值" min-width="130" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openEdit('dictItem', row)">编辑</el-button>
                  <el-button link type="danger" @click="remove('dictItem', row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div v-if="dialogKind !== 'menu' && dialogKind !== 'dictItem'" class="form-grid">
          <el-form-item :label="dialogKind === 'permission' ? '权限编码' : '编码'" prop="code">
            <el-input v-model="form.code" maxlength="100" />
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" maxlength="100" />
          </el-form-item>
        </div>

        <template v-if="dialogKind === 'organization'">
          <div class="form-grid">
            <el-form-item label="上级组织">
              <el-select v-model="form.parentId" clearable filterable class="full-width">
                <el-option v-for="item in organizations" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="组织类型">
              <el-select v-model="form.type" class="full-width">
                <el-option label="公司" value="COMPANY" />
                <el-option label="部门" value="DEPARTMENT" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-grid">
            <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
            <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
          </div>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        </template>

        <template v-if="dialogKind === 'module'">
          <el-form-item label="路由前缀"><el-input v-model="form.routePrefix" placeholder="/api/admin/identity" /></el-form-item>
        </template>

        <template v-if="dialogKind === 'permission'">
          <el-form-item label="所属模块">
            <el-select v-model="form.moduleId" clearable filterable class="full-width">
              <el-option v-for="item in modules" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" />
            </el-select>
          </el-form-item>
        </template>

        <template v-if="dialogKind === 'role'">
          <el-form-item label="权限" prop="permissionIds">
            <el-select v-model="form.permissionIds" multiple filterable class="full-width">
              <el-option v-for="item in permissions" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" />
            </el-select>
          </el-form-item>
        </template>

        <template v-if="dialogKind === 'menu'">
          <div class="form-grid">
            <el-form-item label="菜单名称" prop="name"><el-input v-model="form.name" /></el-form-item>
            <el-form-item label="所属模块">
              <el-select v-model="form.moduleId" clearable filterable class="full-width">
                <el-option v-for="item in modules" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-grid">
            <el-form-item label="上级菜单">
              <el-select v-model="form.parentId" clearable filterable class="full-width">
                <el-option v-for="item in availableParentMenus" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="权限标识">
              <el-select v-model="form.permissionCode" clearable filterable allow-create class="full-width">
                <el-option v-for="item in permissions" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.code" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-grid">
            <el-form-item label="网关路由"><el-input v-model="form.path" placeholder="/admin/identity/menus" /></el-form-item>
            <el-form-item label="前端组件"><el-input v-model="form.component" placeholder="IdentityManagementView" /></el-form-item>
          </div>
          <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        </template>

        <template v-if="dialogKind === 'dictItem'">
          <div class="form-grid">
            <el-form-item label="所属字典">
              <el-select v-model="form.typeId" class="full-width">
                <el-option v-for="item in dictionaryTypes" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签" prop="label"><el-input v-model="form.label" /></el-form-item>
          </div>
          <el-form-item label="值" prop="value"><el-input v-model="form.value" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
        </template>

        <el-form-item v-if="!['dictItem', 'menu'].includes(dialogKind)" label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <div class="form-grid">
          <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item v-if="dialogKind === 'menu'" label="菜单可见">
          <el-switch v-model="form.visible" active-text="可见" inactive-text="隐藏" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
