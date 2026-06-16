import { readonly, ref } from 'vue'

export type AppLocale = 'zh-CN' | 'en'

const storedLocale = localStorage.getItem('platform-locale') || localStorage.getItem('issue-tracker-locale')
const initialLocale: AppLocale = storedLocale === 'en' ? 'en' : 'zh-CN'

const messages = {
  'zh-CN': {
    app: {
      name: '系统管理平台',
      center: '系统管理中心',
      workspace: '工作台',
      logout: '退出登录',
      language: 'English',
    },
    platform: {
      title: '统一业务平台',
      issue: '问题跟踪',
      identity: '系统管理',
      currentSystem: '当前系统',
      switchSystem: '系统切换',
      online: '在线',
      expandSidebar: '展开菜单',
      collapseSidebar: '收起菜单',
      userRoles: '用户角色',
    },
    common: {
      search: '查询',
      cancel: '取消',
      save: '保存',
      edit: '编辑',
      delete: '删除',
      enabled: '启用',
      disabled: '停用',
      operation: '操作',
      noData: '暂无数据',
    },
    nav: {
      users: '用户与权限',
      identityConfig: '系统配置',
      login: '登录',
      register: '注册',
    },
    auth: {
      eyebrow: '平台身份认证',
      heroTitle: '统一身份认证\n连接所有业务系统',
      heroIntro: '一次登录后可访问问题跟踪和后续接入的业务系统，角色权限由系统管理中心统一维护。',
      sso: '统一登录',
      rbac: '角色授权',
      microservice: '微服务接入',
      welcome: '欢迎回来',
      loginTitle: '登录统一平台',
      loginSubtitle: '使用统一身份认证账号继续',
      username: '用户名',
      password: '密码',
      usernamePlaceholder: '请输入用户名',
      passwordPlaceholder: '请输入密码',
      usernameRequired: '请输入用户名',
      passwordRequired: '请输入密码',
      loginButton: '登录',
      loginSuccess: '登录成功',
      noAccount: '还没有账号？',
      registerNow: '立即注册',
      registerEyebrow: '加入统一平台',
      registerHeroTitle: '创建统一账号\n访问授权系统',
      registerHeroIntro: '注册后可使用统一账号进入各业务系统，后续权限由管理员在身份认证中心分配。',
      createAccount: '创建账号',
      registerTitle: '注册新账号',
      confirmPassword: '确认密码',
      confirmPasswordRequired: '请再次输入密码',
      passwordMismatch: '两次输入的密码不一致',
      passwordMin: '密码至少 8 位',
      passwordPattern: '密码需同时包含字母和数字',
      registerSuccess: '注册成功',
      alreadyAccount: '已有账号？',
      backToLogin: '返回登录',
    },
    user: {
      title: '用户与角色',
      accessControl: '访问控制',
      add: '新增用户',
      edit: '编辑用户',
      searchPlaceholder: '搜索用户名、显示名称或邮箱',
      username: '用户名',
      displayName: '显示名称',
      email: '邮箱',
      roles: '角色',
      createdAt: '创建时间',
      enabled: '启用',
      password: '登录密码',
      resetPassword: '重置密码（留空不修改）',
      accountStatus: '账号状态',
      save: '保存',
      usernameRequired: '请输入用户名',
      usernamePattern: '4-50 位字母、数字或下划线',
      emailRequired: '请输入邮箱',
      emailInvalid: '邮箱格式不正确',
      displayNameRequired: '请输入显示名称',
      passwordRequiredOnCreate: '新增用户必须设置密码',
      passwordPattern: '密码至少 8 位且同时包含字母和数字',
      roleRequired: '至少选择一个角色',
      loadRolesFailed: '角色数据加载失败：{message}',
      created: '用户已创建，可以使用该账号登录',
      updated: '用户已更新',
      enabledMessage: '用户已启用',
      disabledMessage: '用户已停用',
      deleteTitle: '删除用户',
      deleteConfirm: '确认删除用户 {name}（{username}）？历史记录会保留，该账号将无法登录。',
      deleted: '用户已删除',
    },
  },
  en: {
    app: {
      name: 'System Management Platform',
      center: 'System Management Center',
      workspace: 'Workspace',
      logout: 'Sign out',
      language: '中文',
    },
    platform: {
      title: 'Unified Operations Platform',
      issue: 'Issue Tracker',
      identity: 'System Management',
      currentSystem: 'Current System',
      switchSystem: 'Switch System',
      online: 'Online',
      expandSidebar: 'Expand Menu',
      collapseSidebar: 'Collapse Menu',
      userRoles: 'User Roles',
    },
    common: {
      search: 'Search',
      cancel: 'Cancel',
      save: 'Save',
      edit: 'Edit',
      delete: 'Delete',
      enabled: 'Enabled',
      disabled: 'Disabled',
      operation: 'Actions',
      noData: 'No data',
    },
    nav: {
      users: 'Users & Access',
      identityConfig: 'System Config',
      login: 'Sign In',
      register: 'Register',
    },
    auth: {
      eyebrow: 'Platform Identity',
      heroTitle: 'Unified identity\nfor every business system',
      heroIntro: 'Sign in once to access Issue Tracker and future business systems. Roles and permissions are maintained centrally in System Management.',
      sso: 'Unified Login',
      rbac: 'Role Authorization',
      microservice: 'Microservice Access',
      welcome: 'Welcome Back',
      loginTitle: 'Sign in to the platform',
      loginSubtitle: 'Continue with your unified identity account',
      username: 'Username',
      password: 'Password',
      usernamePlaceholder: 'Enter username',
      passwordPlaceholder: 'Enter password',
      usernameRequired: 'Username is required',
      passwordRequired: 'Password is required',
      loginButton: 'Sign In',
      loginSuccess: 'Signed in',
      noAccount: 'No account yet?',
      registerNow: 'Register now',
      registerEyebrow: 'Join the platform',
      registerHeroTitle: 'Create one account\nfor authorized systems',
      registerHeroIntro: 'After registration, use your unified account to enter business systems. Administrators can assign roles in System Management.',
      createAccount: 'Create Account',
      registerTitle: 'Register a New Account',
      confirmPassword: 'Confirm Password',
      confirmPasswordRequired: 'Please enter the password again',
      passwordMismatch: 'The two passwords do not match',
      passwordMin: 'Password must be at least 8 characters',
      passwordPattern: 'Password must include both letters and digits',
      registerSuccess: 'Registration successful',
      alreadyAccount: 'Already have an account?',
      backToLogin: 'Back to sign in',
    },
    user: {
      title: 'Users and Roles',
      accessControl: 'Access Control',
      add: 'Add User',
      edit: 'Edit User',
      searchPlaceholder: 'Search username, display name, or email',
      username: 'Username',
      displayName: 'Display Name',
      email: 'Email',
      roles: 'Roles',
      createdAt: 'Created At',
      enabled: 'Enabled',
      password: 'Login Password',
      resetPassword: 'Reset Password (leave blank to keep)',
      accountStatus: 'Account Status',
      save: 'Save',
      usernameRequired: 'Username is required',
      usernamePattern: 'Use 4-50 letters, digits, or underscores',
      emailRequired: 'Email is required',
      emailInvalid: 'Invalid email address',
      displayNameRequired: 'Display name is required',
      passwordRequiredOnCreate: 'Password is required for new users',
      passwordPattern: 'Password must be at least 8 characters and include letters and digits',
      roleRequired: 'Select at least one role',
      loadRolesFailed: 'Failed to load roles: {message}',
      created: 'User created and can sign in now',
      updated: 'User updated',
      enabledMessage: 'User enabled',
      disabledMessage: 'User disabled',
      deleteTitle: 'Delete User',
      deleteConfirm: 'Delete user {name} ({username})? Historical records will be kept, but this account can no longer sign in.',
      deleted: 'User deleted',
    },
  },
} as const

const locale = ref<AppLocale>(initialLocale)

function resolveMessage(targetLocale: AppLocale, path: string): string | undefined {
  let current: unknown = messages[targetLocale]
  for (const key of path.split('.')) {
    if (!current || typeof current !== 'object' || !(key in current)) return undefined
    current = (current as Record<string, unknown>)[key]
  }
  return typeof current === 'string' ? current : undefined
}

export function useAppI18n() {
  function t(path: string) {
    return resolveMessage(locale.value, path)
      || resolveMessage('zh-CN', path)
      || path
  }
  return { locale: readonly(locale), t }
}

export function setAppLocale(locale: AppLocale) {
  currentLocale(locale)
}

function currentLocale(value: AppLocale) {
  locale.value = value
  localStorage.setItem('platform-locale', value)
  localStorage.setItem('issue-tracker-locale', value)
  document.documentElement.lang = value
}

document.documentElement.lang = initialLocale
