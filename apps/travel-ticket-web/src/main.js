const { createApp, computed, onMounted, reactive, ref } = window.Vue;

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const PROFILE_KEY = "userProfile";
const LOCALE_KEY = "platform-locale";
const apiBase = "";

const travelMessages = {
  "zh-CN": {
    platform: {
      title: "统一业务平台",
      issue: "问题跟踪",
      travel: "出差车票",
      identity: "身份认证",
      currentSystem: "当前系统",
      switchSystem: "系统切换",
      online: "在线",
      expandSidebar: "展开菜单",
      collapseSidebar: "收起菜单",
    },
    auth: {
      eyebrow: "Travel Ticket Ops",
      title: "出差车票管理系统",
      redirecting: "正在跳转到统一身份认证中心...",
      login: "统一登录",
    },
    nav: {
      dashboard: "总览",
      tickets: "车票",
      approvals: "审批",
      risk: "风控",
    },
    common: {
      language: "English",
      signOut: "退出登录",
      refresh: "刷新",
      seed: "导入演示数据",
      reindex: "重建 ES 索引",
      exportCsv: "导出 CSV",
      newTicket: "新增车票",
      search: "查询",
      reset: "重置",
      edit: "编辑",
      delete: "删除",
      readonly: "只读",
      save: "保存",
      cancel: "取消",
      approve: "通过",
      reject: "驳回",
      returnTicket: "补票",
      reimburse: "核销",
      firstPage: "首页",
      prevPage: "上一页",
      nextPage: "下一页",
      lastPage: "末页",
      pageSize: "每页",
      all: "全部",
      total: "共 {total} 条",
      page: "第 {page} / {pages} 页",
      filtered: "本页筛选 {count} 条",
    },
    section: {
      eyebrow: "运营控制台",
      dashboardTitle: "出差车票归集、审批与核销",
      ticketsTitle: "车票记录管理",
      approvalsTitle: "待审批车票处理",
      riskTitle: "风险预警复核",
      dashboardHint: "租户：{tenantId} · 数据来自 PostgreSQL，热点快照写入 Redis，搜索索引写入 Elasticsearch",
      ticketsHint: "分页查看、筛选、编辑和删除车票记录",
      approvalsHint: "按待审批状态分页处理车票审批动作",
      riskHint: "按风险等级分页查看需要复核的车票",
    },
    metrics: {
      total: "车票总数",
      totalHint: "PostgreSQL 持久化记录",
      pendingAmount: "待处理金额",
      pendingHint: "待审批、待补票、异常",
      riskRate: "风险率",
      riskHint: "高额或凭证缺失",
      approved: "已通过",
      approvedHint: "已通过与已核销",
    },
    filters: {
      aria: "车票筛选",
      search: "搜索",
      searchPlaceholder: "员工 / 票号 / 城市 / 车次",
      status: "状态",
      city: "城市",
      ticketType: "票种",
    },
    panel: {
      ticketPool: "车票池",
      ticketRecords: "车票记录",
      approvalQueue: "审批队列",
      pendingTickets: "待审批车票",
      riskWarning: "风险预警",
      needsReview: "需要复核",
      noPending: "暂无待审批车票",
      noRisk: "暂无风险车票",
    },
    table: {
      employee: "员工",
      route: "路线",
      ticket: "票据",
      amount: "金额",
      status: "状态",
      risk: "风险",
      actions: "操作",
    },
    dialog: {
      editEyebrow: "编辑记录",
      newEyebrow: "新增记录",
      editTitle: "编辑出差车票",
      newTitle: "新增出差车票",
      employeeId: "员工工号",
      employeeName: "员工姓名",
      department: "部门",
      tripPurpose: "出差事由",
      ticketType: "票种",
      ticketNo: "票号",
      carrierNo: "车次/航班",
      departureCity: "出发城市",
      arrivalCity: "到达城市",
      departureAt: "出发时间",
      seatClass: "座席",
      amount: "金额",
      status: "状态",
      attachment: "凭证",
      deleteEyebrow: "删除确认",
      deleteTitle: "确认删除这张车票？",
      deleteBody: "{employee} 的 {carrier} 车票 {ticketNo} 将从业务库删除，并同步清理 Redis 快照与 ES 索引。",
      confirmDelete: "确认删除",
    },
    status: {
      ALL: "全部",
      PENDING_REVIEW: "待审批",
      APPROVED: "已通过",
      REJECTED: "已驳回",
      MISSING_ATTACHMENT: "待补票",
      EXCEPTION: "异常",
      REIMBURSED: "已核销",
    },
    risk: {
      NONE: "无",
      LOW: "低",
      MEDIUM: "中",
      HIGH: "高",
      CRITICAL: "严重",
    },
    ticketType: {
      ALL: "全部",
      HIGH_SPEED_RAIL: "高铁",
      EMU: "动车",
      TRAIN: "普铁",
      INTERCITY: "城际",
      FLIGHT: "飞机",
      OTHER: "其他",
    },
    attachment: {
      UPLOADED: "已上传",
      MISSING: "缺失",
    },
    city: {
      全部: "全部",
      北京: "北京",
      上海: "上海",
      广州: "广州",
      深圳: "深圳",
      杭州: "杭州",
      成都: "成都",
      武汉: "武汉",
      南京: "南京",
      西安: "西安",
      重庆: "重庆",
      天津: "天津",
      苏州: "苏州",
      厦门: "厦门",
      青岛: "青岛",
      郑州: "郑州",
      长沙: "长沙",
      合肥: "合肥",
      宁波: "宁波",
      福州: "福州",
      昆明: "昆明",
    },
    message: {
      forbidden: "当前账号没有该车票操作权限，请在身份认证中心分配车票角色",
      requestFailed: "请求失败：{status}",
      noApprovePermission: "当前账号没有车票审批权限",
      noCreatePermission: "当前账号没有新增车票权限",
      noEditPermission: "当前账号没有编辑车票权限",
      noDeletePermission: "当前账号没有删除车票权限",
      noReindexPermission: "当前账号没有重建索引权限",
      updated: "车票已更新，并同步写入 Redis 与 ES",
      created: "车票已新增，并同步写入 Redis 与 ES",
      statusUpdated: "车票已更新为{status}",
      deleted: "车票已删除，并清理 Redis 与 ES",
      seeded: "已导入 {count} 条演示数据",
      seedExists: "演示数据已存在",
      reindexed: "已重建 {count} 条 ES 索引",
      exported: "CSV 已导出",
    },
    csv: {
      ticketNo: "票号",
      employee: "员工",
      department: "部门",
      route: "路线",
      carrier: "车次",
      amount: "金额",
      status: "状态",
      risk: "风险",
      fileName: "travel-tickets.csv",
    },
  },
  en: {
    platform: {
      title: "Unified Operations Platform",
      issue: "Issue Tracker",
      travel: "Travel Tickets",
      identity: "Identity Center",
      currentSystem: "Current System",
      switchSystem: "Switch System",
      online: "Online",
      expandSidebar: "Expand Menu",
      collapseSidebar: "Collapse Menu",
    },
    auth: {
      eyebrow: "Travel Ticket Ops",
      title: "Travel Ticket Management",
      redirecting: "Redirecting to the unified identity center...",
      login: "Unified Sign In",
    },
    nav: {
      dashboard: "Dashboard",
      tickets: "Tickets",
      approvals: "Approvals",
      risk: "Risk",
    },
    common: {
      language: "中文",
      signOut: "Sign out",
      refresh: "Refresh",
      seed: "Import Demo Data",
      reindex: "Rebuild ES Index",
      exportCsv: "Export CSV",
      newTicket: "New Ticket",
      search: "Search",
      reset: "Reset",
      edit: "Edit",
      delete: "Delete",
      readonly: "Read only",
      save: "Save",
      cancel: "Cancel",
      approve: "Approve",
      reject: "Reject",
      returnTicket: "Return",
      reimburse: "Reimburse",
      firstPage: "First",
      prevPage: "Previous",
      nextPage: "Next",
      lastPage: "Last",
      pageSize: "Page size",
      all: "All",
      total: "{total} total",
      page: "Page {page} / {pages}",
      filtered: "{count} on this page",
    },
    section: {
      eyebrow: "Operations Console",
      dashboardTitle: "Travel ticket collection, approval, and reimbursement",
      ticketsTitle: "Ticket Records",
      approvalsTitle: "Pending Ticket Approvals",
      riskTitle: "Risk Review",
      dashboardHint: "Tenant: {tenantId} · Data is stored in PostgreSQL, hot snapshots in Redis, and search indexes in Elasticsearch",
      ticketsHint: "Page, filter, edit, and delete travel ticket records",
      approvalsHint: "Process pending ticket approval actions page by page",
      riskHint: "Review tickets that require attention by risk level",
    },
    metrics: {
      total: "Total Tickets",
      totalHint: "Persisted in PostgreSQL",
      pendingAmount: "Pending Amount",
      pendingHint: "Pending, missing attachment, or exception",
      riskRate: "Risk Rate",
      riskHint: "High amount or missing voucher",
      approved: "Approved",
      approvedHint: "Approved and reimbursed",
    },
    filters: {
      aria: "Ticket filters",
      search: "Search",
      searchPlaceholder: "Employee / ticket no. / city / train",
      status: "Status",
      city: "City",
      ticketType: "Type",
    },
    panel: {
      ticketPool: "Ticket Pool",
      ticketRecords: "Ticket Records",
      approvalQueue: "Approval Queue",
      pendingTickets: "Pending Tickets",
      riskWarning: "Risk Warning",
      needsReview: "Needs Review",
      noPending: "No pending tickets",
      noRisk: "No risky tickets",
    },
    table: {
      employee: "Employee",
      route: "Route",
      ticket: "Ticket",
      amount: "Amount",
      status: "Status",
      risk: "Risk",
      actions: "Actions",
    },
    dialog: {
      editEyebrow: "Edit Record",
      newEyebrow: "New Record",
      editTitle: "Edit Travel Ticket",
      newTitle: "New Travel Ticket",
      employeeId: "Employee ID",
      employeeName: "Employee Name",
      department: "Department",
      tripPurpose: "Trip Purpose",
      ticketType: "Ticket Type",
      ticketNo: "Ticket No.",
      carrierNo: "Train/Flight",
      departureCity: "Departure City",
      arrivalCity: "Arrival City",
      departureAt: "Departure Time",
      seatClass: "Seat Class",
      amount: "Amount",
      status: "Status",
      attachment: "Voucher",
      deleteEyebrow: "Delete Confirmation",
      deleteTitle: "Delete this ticket?",
      deleteBody: "{employee}'s {carrier} ticket {ticketNo} will be deleted from the business database, and Redis snapshots and ES indexes will be cleaned.",
      confirmDelete: "Delete",
    },
    status: {
      ALL: "All",
      PENDING_REVIEW: "Pending",
      APPROVED: "Approved",
      REJECTED: "Rejected",
      MISSING_ATTACHMENT: "Missing Voucher",
      EXCEPTION: "Exception",
      REIMBURSED: "Reimbursed",
    },
    risk: {
      NONE: "None",
      LOW: "Low",
      MEDIUM: "Medium",
      HIGH: "High",
      CRITICAL: "Critical",
    },
    ticketType: {
      ALL: "All",
      HIGH_SPEED_RAIL: "High-speed Rail",
      EMU: "EMU",
      TRAIN: "Train",
      INTERCITY: "Intercity",
      FLIGHT: "Flight",
      OTHER: "Other",
    },
    attachment: {
      UPLOADED: "Uploaded",
      MISSING: "Missing",
    },
    city: {
      全部: "All",
      北京: "Beijing",
      上海: "Shanghai",
      广州: "Guangzhou",
      深圳: "Shenzhen",
      杭州: "Hangzhou",
      成都: "Chengdu",
      武汉: "Wuhan",
      南京: "Nanjing",
      西安: "Xi'an",
      重庆: "Chongqing",
      天津: "Tianjin",
      苏州: "Suzhou",
      厦门: "Xiamen",
      青岛: "Qingdao",
      郑州: "Zhengzhou",
      长沙: "Changsha",
      合肥: "Hefei",
      宁波: "Ningbo",
      福州: "Fuzhou",
      昆明: "Kunming",
    },
    message: {
      forbidden: "This account does not have permission for this travel-ticket action. Assign a travel-ticket role in the Identity Center.",
      requestFailed: "Request failed: {status}",
      noApprovePermission: "This account cannot approve travel tickets",
      noCreatePermission: "This account cannot create travel tickets",
      noEditPermission: "This account cannot edit travel tickets",
      noDeletePermission: "This account cannot delete travel tickets",
      noReindexPermission: "This account cannot rebuild search indexes",
      updated: "Ticket updated and synchronized to Redis and ES",
      created: "Ticket created and synchronized to Redis and ES",
      statusUpdated: "Ticket status updated to {status}",
      deleted: "Ticket deleted, with Redis and ES cleaned",
      seeded: "Imported {count} demo records",
      seedExists: "Demo data already exists",
      reindexed: "Rebuilt {count} ES indexes",
      exported: "CSV exported",
    },
    csv: {
      ticketNo: "Ticket No.",
      employee: "Employee",
      department: "Department",
      route: "Route",
      carrier: "Train",
      amount: "Amount",
      status: "Status",
      risk: "Risk",
      fileName: "travel-tickets.csv",
    },
  },
};

function readLocale() {
  return (localStorage.getItem(LOCALE_KEY) || localStorage.getItem("issue-tracker-locale")) === "en" ? "en" : "zh-CN";
}

function resolveMessage(locale, path) {
  return path.split(".").reduce((current, key) => current?.[key], travelMessages[locale]);
}

const statusEnum = {
  全部: "",
  待审批: "PENDING_REVIEW",
  已通过: "APPROVED",
  已驳回: "REJECTED",
  待补票: "MISSING_ATTACHMENT",
  异常: "EXCEPTION",
  已核销: "REIMBURSED",
};

const statusLabel = Object.fromEntries(Object.entries(statusEnum).map(([label, value]) => [value, label]));

const riskLabel = {
  NONE: "无",
  LOW: "低",
  MEDIUM: "中",
  HIGH: "高",
  CRITICAL: "严重",
};

const riskEnum = Object.fromEntries(Object.entries(riskLabel).map(([value, label]) => [label, value]));

const ticketTypeEnum = {
  高铁: "HIGH_SPEED_RAIL",
  动车: "EMU",
  普铁: "TRAIN",
  城际: "INTERCITY",
  飞机: "FLIGHT",
  其他: "OTHER",
};

const ticketTypeLabel = Object.fromEntries(Object.entries(ticketTypeEnum).map(([label, value]) => [value, label]));

const cityOptions = [
  "全部",
  "北京",
  "上海",
  "广州",
  "深圳",
  "杭州",
  "成都",
  "武汉",
  "南京",
  "西安",
  "重庆",
  "天津",
  "苏州",
  "厦门",
  "青岛",
  "郑州",
  "长沙",
  "合肥",
  "宁波",
  "福州",
  "昆明",
];

const attachmentEnum = {
  已上传: "UPLOADED",
  缺失: "MISSING",
};

const attachmentLabel = {
  UPLOADED: "已上传",
  MISSING: "缺失",
};

const sampleTickets = [
  {
    employeeId: 10086,
    employeeName: "沈韵",
    department: "华东销售部",
    tripPurpose: "上海重点客户拜访",
    ticketType: "高铁",
    ticketNo: "G12-20260610-001",
    carrierNo: "G12",
    departureCity: "上海",
    arrivalCity: "北京",
    departureAt: "2026-06-10T09:30",
    seatClass: "二等座",
    amount: 553,
    status: "待审批",
    attachmentStatus: "已上传",
  },
  {
    employeeId: 20018,
    employeeName: "罗启",
    department: "交付中心",
    tripPurpose: "项目验收",
    ticketType: "动车",
    ticketNo: "D2282-20260610-002",
    carrierNo: "D2282",
    departureCity: "深圳",
    arrivalCity: "杭州",
    departureAt: "2026-06-10T10:15",
    seatClass: "一等座",
    amount: 468,
    status: "已核销",
    attachmentStatus: "已上传",
  },
  {
    employeeId: 30027,
    employeeName: "陈伊",
    department: "财务共享",
    tripPurpose: "总部会议",
    ticketType: "高铁",
    ticketNo: "G17-20260610-003",
    carrierNo: "G17",
    departureCity: "北京",
    arrivalCity: "上海",
    departureAt: "2026-06-10T13:20",
    seatClass: "商务座",
    amount: 1667,
    status: "异常",
    attachmentStatus: "已上传",
  },
  {
    employeeId: 40032,
    employeeName: "唐硕",
    department: "北区运营",
    tripPurpose: "门店巡检",
    ticketType: "城际",
    ticketNo: "C812-20260610-004",
    carrierNo: "C812",
    departureCity: "天津",
    arrivalCity: "北京",
    departureAt: "2026-06-11T07:50",
    seatClass: "二等座",
    amount: 68,
    status: "待补票",
    attachmentStatus: "缺失",
  },
];

function readJson(key, fallback) {
  try {
    const stored = localStorage.getItem(key);
    return stored ? JSON.parse(stored) : fallback;
  } catch {
    return fallback;
  }
}

function readPlatformUser() {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  const profile = readJson(PROFILE_KEY, null);
  if (!token || !profile?.id) {
    return null;
  }
  return {
    id: profile.id,
    tenantId: profile.id,
    name: profile.displayName || profile.username,
    company: "统一业务平台",
    email: profile.email,
    username: profile.username,
    roles: profile.roles || [],
    permissions: profile.permissions || [],
  };
}

function emptyTicketForm() {
  return {
    employeeId: "",
    employeeName: "",
    department: "",
    tripPurpose: "",
    ticketType: "高铁",
    ticketNo: "",
    carrierNo: "",
    departureCity: "",
    arrivalCity: "",
    departureAt: "",
    seatClass: "二等座",
    amount: "",
    status: "待审批",
    attachmentStatus: "已上传",
  };
}

function toInstant(value) {
  if (!value) {
    return null;
  }
  return new Date(value).toISOString();
}

function toLocalDateTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function toEmployeeId(value) {
  const digits = String(value).replace(/\D/g, "");
  return Number(digits || value);
}

function mapTicket(item) {
  return {
    id: item.id || item.ticketId,
    tenantId: item.tenantId,
    employeeId: item.employeeId,
    employeeName: item.employeeName || `员工${item.employeeId}`,
    department: item.department || "未分配部门",
    tripPurpose: item.tripPurpose || "未填写事由",
    ticketType: ticketTypeLabel[item.travelType] || item.travelType || "其他",
    ticketNo: item.ticketNo,
    carrierNo: item.carrierNo,
    departureCity: item.departureCity,
    arrivalCity: item.arrivalCity,
    departureAt: toLocalDateTime(item.departAt),
    arriveAt: toLocalDateTime(item.arriveAt),
    seatClass: item.seatClass || "",
    amount: Number(item.amount || 0),
    currency: item.currency || "CNY",
    status: statusLabel[item.status] || item.status || "待审批",
    attachmentStatus: attachmentLabel[item.attachmentStatus] || item.attachmentStatus || "已上传",
    riskLevel: riskLabel[item.riskLevel] || item.riskLevel || "无",
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

function toPayload(form) {
  return {
    employeeId: toEmployeeId(form.employeeId),
    employeeName: form.employeeName.trim(),
    department: form.department.trim(),
    ticketNo: form.ticketNo.trim(),
    externalSource: "WEB",
    externalTicketId: form.ticketNo.trim(),
    travelType: ticketTypeEnum[form.ticketType] || "OTHER",
    departureCity: form.departureCity.trim(),
    arrivalCity: form.arrivalCity.trim(),
    carrierNo: form.carrierNo.trim(),
    seatClass: form.seatClass.trim(),
    tripPurpose: form.tripPurpose.trim(),
    attachmentStatus: attachmentEnum[form.attachmentStatus] || "UPLOADED",
    departAt: toInstant(form.departureAt),
    arriveAt: null,
    amount: Number(form.amount),
    currency: "CNY",
    status: statusEnum[form.status] || "PENDING_REVIEW",
  };
}

function pageCount(pager) {
  return Math.max(1, Math.ceil(Number(pager.total || 0) / Number(pager.size || 1)));
}

function pageRange(pager, visibleCount) {
  if (!pager.total || !visibleCount) {
    return "0-0";
  }
  const start = pager.page * pager.size + 1;
  const end = Math.min(pager.total, start + visibleCount - 1);
  return `${start}-${end}`;
}

createApp({
  setup() {
    const locale = ref(readLocale());
    document.documentElement.lang = locale.value;
    const currentUser = ref(readPlatformUser());
    const activeSection = ref("dashboard");
    const sidebarCollapsed = ref(localStorage.getItem("platform-sidebar-collapsed") === "true");
    const toast = ref("");
    const editingId = ref("");
    const showTicketForm = ref(false);
    const deleteTarget = ref(null);
    const loading = ref(false);
    const tickets = ref([]);
    const pendingTickets = ref([]);
    const riskEvents = ref([]);

    const metrics = reactive({
      total: 0,
      pendingAmount: 0,
      riskRate: 0,
      approved: 0,
    });

    const filters = reactive({
      query: "",
      status: "全部",
      city: "全部",
      ticketType: "全部",
    });

    const ticketPager = reactive({
      page: 0,
      size: 20,
      total: 0,
    });

    const pendingPager = reactive({
      page: 0,
      size: 20,
      total: 0,
    });

    const riskPager = reactive({
      page: 0,
      size: 20,
      total: 0,
    });

    const ticketForm = reactive(emptyTicketForm());
    const statuses = ["全部", "待审批", "已通过", "已驳回", "待补票", "异常", "已核销"];
    const ticketTypes = ["全部", "高铁", "动车", "普铁", "城际", "飞机", "其他"];
    const editableStatuses = computed(() => statuses.filter((status) => status !== "全部"));
    const editableTicketTypes = computed(() => ticketTypes.filter((type) => type !== "全部"));
    const isDashboard = computed(() => activeSection.value === "dashboard");
    const showTicketModule = computed(() => isDashboard.value || activeSection.value === "tickets");
    const showApprovalModule = computed(() => isDashboard.value || activeSection.value === "approvals");
    const showRiskModule = computed(() => isDashboard.value || activeSection.value === "risk");
    const hasRole = (role) => currentUser.value?.roles?.includes(role) || false;
    const hasAnyRole = (...roles) => roles.some((role) => hasRole(role));
    const hasPermission = (permission) => currentUser.value?.permissions?.includes(permission) || false;
    const isTravelAdmin = computed(() => hasAnyRole("ADMIN", "TRAVEL_ADMIN"));
    const canManageIdentity = computed(() => hasRole("ADMIN") || hasPermission("user:manage"));
    const canCreateTicket = computed(() => isTravelAdmin.value || hasRole("TRAVEL_USER") || hasPermission("travel:ticket:create"));
    const canEditTickets = computed(() => isTravelAdmin.value || hasAnyRole("TRAVEL_USER", "TRAVEL_APPROVER") || hasPermission("travel:ticket:update"));
    const canDeleteTickets = computed(() => isTravelAdmin.value || hasPermission("travel:ticket:delete"));
    const canApproveTickets = computed(() => isTravelAdmin.value || hasRole("TRAVEL_APPROVER") || hasPermission("travel:ticket:approve"));
    const canReindexSearch = computed(() => isTravelAdmin.value || hasPermission("travel:search:reindex"));
    function t(path) {
      return resolveMessage(locale.value, path) || resolveMessage("zh-CN", path) || path;
    }

    function format(path, vars = {}) {
      return Object.entries(vars).reduce(
        (message, [key, value]) => message.replace(`{${key}}`, String(value)),
        t(path),
      );
    }

    function setLocale(value) {
      locale.value = value === "en" ? "en" : "zh-CN";
      localStorage.setItem(LOCALE_KEY, locale.value);
      localStorage.setItem("issue-tracker-locale", locale.value);
      document.documentElement.lang = locale.value;
    }

    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value;
      localStorage.setItem("platform-sidebar-collapsed", String(sidebarCollapsed.value));
    }

    function statusText(status) {
      const key = status === "全部" ? "ALL" : statusEnum[status] || status;
      return t(`status.${key}`) || status;
    }

    function riskText(risk) {
      const key = riskEnum[risk] || risk;
      return t(`risk.${key}`) || risk;
    }

    function ticketTypeText(type) {
      const key = type === "全部" ? "ALL" : ticketTypeEnum[type] || type;
      return t(`ticketType.${key}`) || type;
    }

    function cityText(city) {
      return t(`city.${city}`) || city;
    }

    function attachmentText(status) {
      const key = attachmentEnum[status] || status;
      return t(`attachment.${key}`) || status;
    }

    const sectionTitle = computed(() => ({
      dashboard: t("section.dashboardTitle"),
      tickets: t("section.ticketsTitle"),
      approvals: t("section.approvalsTitle"),
      risk: t("section.riskTitle"),
    }[activeSection.value]));
    const cities = cityOptions;

    const filteredTickets = computed(() => {
      const keyword = filters.query.trim().toLowerCase();
      return tickets.value.filter((ticket) => {
        const text = [
          ticket.employeeName,
          ticket.employeeId,
          ticket.department,
          ticket.ticketNo,
          ticket.carrierNo,
          ticket.departureCity,
          ticket.arrivalCity,
          ticket.tripPurpose,
        ]
          .join(" ")
          .toLowerCase();
        const queryMatched = !keyword || text.includes(keyword);
        const statusMatched = filters.status === "全部" || ticket.status === filters.status;
        const cityMatched =
          filters.city === "全部" || ticket.departureCity === filters.city || ticket.arrivalCity === filters.city;
        const typeMatched = filters.ticketType === "全部" || ticket.ticketType === filters.ticketType;
        return queryMatched && statusMatched && cityMatched && typeMatched;
      });
    });

    const ticketPageCount = computed(() => pageCount(ticketPager));
    const pendingPageCount = computed(() => pageCount(pendingPager));
    const riskPageCount = computed(() => pageCount(riskPager));
    const ticketRangeText = computed(() => pageRange(ticketPager, tickets.value.length));
    const pendingRangeText = computed(() => pageRange(pendingPager, pendingTickets.value.length));
    const riskRangeText = computed(() => pageRange(riskPager, riskEvents.value.length));

    const riskTickets = computed(() => {
      return riskEvents.value.map((event) => ({
        id: event.ticketId,
        employeeName: event.employeeName,
        department: event.department,
        ticketNo: event.ticketNo,
        carrierNo: event.carrierNo,
        departureCity: event.route?.split(" -> ")[0] || "",
        arrivalCity: event.route?.split(" -> ")[1] || "",
        attachmentStatus: attachmentLabel[event.attachmentStatus] || event.attachmentStatus,
        riskLevel: riskLabel[event.riskLevel] || event.riskLevel,
        message: event.message,
      }));
    });

    async function api(path, options = {}) {
      const headers = {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
      };
      const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
      if (accessToken) {
        headers.Authorization = `Bearer ${accessToken}`;
      }

      const response = await fetch(`${apiBase}${path}`, {
        method: options.method || "GET",
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined,
      });
      if ((response.status === 401 || response.status === 403) && !options.retried && await refreshPlatformSession()) {
        return api(path, { ...options, retried: true });
      }
      const payload = await response.json().catch(() => null);
      if (!response.ok || payload?.success === false) {
        if (response.status === 401) {
          clearPlatformSession();
          redirectToLogin();
        }
        if (response.status === 403) {
          throw new Error(t("message.forbidden"));
        }
        throw new Error(payload?.message || format("message.requestFailed", { status: response.status }));
      }
      return payload?.data;
    }

    async function refreshPlatformSession() {
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
      if (!refreshToken) {
        return false;
      }
      const response = await fetch("/api/auth/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });
      if (!response.ok) {
        return false;
      }
      const session = await response.json();
      localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
      localStorage.setItem(PROFILE_KEY, JSON.stringify(session.user));
      currentUser.value = readPlatformUser();
      return true;
    }

    function clearPlatformSession() {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
      localStorage.removeItem(PROFILE_KEY);
      localStorage.removeItem("currentProjectId");
      currentUser.value = null;
    }

    function redirectToLogin() {
      const redirect = encodeURIComponent(`${window.location.pathname}${window.location.search}`);
      window.location.assign(`/login?redirect=${redirect}`);
    }

    function showMessage(message) {
      toast.value = message;
      window.clearTimeout(showMessage.timer);
      showMessage.timer = window.setTimeout(() => {
        toast.value = "";
      }, 2600);
    }

    async function runWithLoading(task) {
      loading.value = true;
      try {
        return await task();
      } finally {
        loading.value = false;
      }
    }

    function applyPage(result, pager, target) {
      target.value = (result?.items || []).map(mapTicket);
      pager.page = Number(result?.page ?? pager.page);
      pager.size = Number(result?.size ?? pager.size);
      pager.total = Number(result?.total ?? 0);
    }

    function applyRiskPage(result) {
      riskEvents.value = result?.items || [];
      riskPager.page = Number(result?.page ?? riskPager.page);
      riskPager.size = Number(result?.size ?? riskPager.size);
      riskPager.total = Number(result?.total ?? 0);
    }

    function appendSharedFilters(query, options = {}) {
      const includeStatus = options.includeStatus !== false;
      const keyword = filters.query.trim();
      const status = statusEnum[filters.status];
      const travelType = ticketTypeEnum[filters.ticketType];
      if (keyword) {
        query.set("q", keyword);
      }
      if (includeStatus && status) {
        query.set("status", status);
      }
      if (filters.city !== "全部") {
        query.set("city", filters.city);
      }
      if (travelType) {
        query.set("travelType", travelType);
      }
      return query;
    }

    async function fetchTicketRecords() {
      const query = new URLSearchParams({
        page: String(ticketPager.page),
        size: String(ticketPager.size),
      });
      return api(`/travel-api/v1/tickets?${appendSharedFilters(query)}`);
    }

    async function fetchPendingApprovals() {
      const query = new URLSearchParams({
        status: "PENDING_REVIEW",
        page: String(pendingPager.page),
        size: String(pendingPager.size),
      });
      return api(`/travel-api/v1/tickets?${appendSharedFilters(query, { includeStatus: false })}`);
    }

    async function fetchRiskEvents() {
      const query = new URLSearchParams({
        page: String(riskPager.page),
        size: String(riskPager.size),
      });
      return api(`/travel-api/v1/risk/events?${appendSharedFilters(query)}`);
    }

    function resetAllPagesAndReload() {
      ticketPager.page = 0;
      pendingPager.page = 0;
      riskPager.page = 0;
      reloadData();
    }

    function resetTicketPageAndReload() {
      resetAllPagesAndReload();
    }

    function clearFilters() {
      filters.query = "";
      filters.status = "全部";
      filters.city = "全部";
      filters.ticketType = "全部";
      resetAllPagesAndReload();
    }

    function resetPendingPageAndReload() {
      pendingPager.page = 0;
      reloadPendingApprovals();
    }

    function resetRiskPageAndReload() {
      riskPager.page = 0;
      reloadRiskEvents();
    }

    function goTicketPage(page) {
      const nextPage = Math.min(Math.max(page, 0), ticketPageCount.value - 1);
      if (nextPage === ticketPager.page) {
        return;
      }
      ticketPager.page = nextPage;
      reloadData();
    }

    function goPendingPage(page) {
      const nextPage = Math.min(Math.max(page, 0), pendingPageCount.value - 1);
      if (nextPage === pendingPager.page) {
        return;
      }
      pendingPager.page = nextPage;
      reloadPendingApprovals();
    }

    function goRiskPage(page) {
      const nextPage = Math.min(Math.max(page, 0), riskPageCount.value - 1);
      if (nextPage === riskPager.page) {
        return;
      }
      riskPager.page = nextPage;
      reloadRiskEvents();
    }

    async function reloadPendingApprovals() {
      await runWithLoading(async () => {
        applyPage(await fetchPendingApprovals(), pendingPager, pendingTickets);
      }).catch((error) => showMessage(error.message));
    }

    async function reloadRiskEvents() {
      await runWithLoading(async () => {
        applyRiskPage(await fetchRiskEvents());
      }).catch((error) => showMessage(error.message));
    }

    async function logout() {
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
      try {
        if (refreshToken) {
          await fetch("/api/auth/logout", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${localStorage.getItem(ACCESS_TOKEN_KEY) || ""}`,
            },
            body: JSON.stringify({ refreshToken }),
          });
        }
      } finally {
        clearPlatformSession();
      }
      activeSection.value = "dashboard";
      tickets.value = [];
      pendingTickets.value = [];
      riskEvents.value = [];
      redirectToLogin();
    }

    function openIssueTracker() {
      window.location.assign("/");
    }

    function openIdentityCenter() {
      window.location.assign("/admin/users");
    }

    function switchSection(section) {
      if (section === "approvals" && !canApproveTickets.value) {
        showMessage(t("message.noApprovePermission"));
        return;
      }
      activeSection.value = section;
    }

    async function reloadData() {
      if (!currentUser.value) {
        return;
      }

      await runWithLoading(async () => {
        const [listResult, pendingResult, riskResult, summary] = await Promise.all([
          fetchTicketRecords(),
          fetchPendingApprovals(),
          fetchRiskEvents(),
          api("/travel-api/v1/reports/summary"),
        ]);

        applyPage(listResult, ticketPager, tickets);
        applyPage(pendingResult, pendingPager, pendingTickets);
        applyRiskPage(riskResult);
        metrics.total = summary?.ticketCount || 0;
        metrics.pendingAmount = Number(summary?.pendingAmount || 0);
        metrics.riskRate = Math.round(Number(summary?.riskRate || 0) * 100);
        metrics.approved = Number(summary?.approvedCount || 0);
      }).catch((error) => showMessage(error.message));
    }

    function resetTicketForm() {
      Object.assign(ticketForm, emptyTicketForm());
      editingId.value = "";
    }

    function openCreateForm() {
      if (!canCreateTicket.value) {
        showMessage(t("message.noCreatePermission"));
        return;
      }
      resetTicketForm();
      showTicketForm.value = true;
    }

    async function editTicket(ticket) {
      await runWithLoading(async () => {
        const detail = await api(`/travel-api/v1/tickets/${ticket.id}`);
        Object.assign(ticketForm, mapTicket(detail));
        editingId.value = ticket.id;
        showTicketForm.value = true;
      }).catch((error) => showMessage(error.message));
    }

    async function saveTicket() {
      if (editingId.value && !canEditTickets.value) {
        showMessage(t("message.noEditPermission"));
        return;
      }
      if (!editingId.value && !canCreateTicket.value) {
        showMessage(t("message.noCreatePermission"));
        return;
      }
      await runWithLoading(async () => {
        const payload = toPayload(ticketForm);
        if (editingId.value) {
          await api(`/travel-api/v1/tickets/${editingId.value}`, {
            method: "PUT",
            body: payload,
          });
          showMessage(t("message.updated"));
        } else {
          await api("/travel-api/v1/tickets", {
            method: "POST",
            body: payload,
          });
          showMessage(t("message.created"));
        }
        showTicketForm.value = false;
        resetTicketForm();
        await reloadData();
      }).catch((error) => showMessage(error.message));
    }

    async function updateStatus(ticket, status) {
      if (!canApproveTickets.value) {
        showMessage(t("message.noApprovePermission"));
        return;
      }
      const action = {
        已通过: "approve",
        已驳回: "reject",
        待补票: "return",
        已核销: "reimburse",
        异常: "exception",
      }[status];
      if (!action) {
        return;
      }
      await runWithLoading(async () => {
        await api(`/travel-api/v1/approvals/tickets/${ticket.id}/actions`, {
          method: "POST",
          body: { action, comment: `前端操作：${status}` },
        });
        showMessage(format("message.statusUpdated", { status: statusText(status) }));
        await reloadData();
      }).catch((error) => showMessage(error.message));
    }

    function removeTicket(ticket) {
      if (!canDeleteTickets.value) {
        showMessage(t("message.noDeletePermission"));
        return;
      }
      deleteTarget.value = ticket;
    }

    function cancelDelete() {
      deleteTarget.value = null;
    }

    async function confirmRemoveTicket() {
      const ticket = deleteTarget.value;
      if (!ticket) {
        return;
      }
      await runWithLoading(async () => {
        await api(`/travel-api/v1/tickets/${ticket.id}`, { method: "DELETE" });
        showMessage(t("message.deleted"));
        deleteTarget.value = null;
        await reloadData();
      }).catch((error) => showMessage(error.message));
    }

    async function seedDemoData() {
      if (!canCreateTicket.value) {
        showMessage(t("message.noCreatePermission"));
        return;
      }
      await runWithLoading(async () => {
        let created = 0;
        for (const ticket of sampleTickets) {
          try {
            await api("/travel-api/v1/tickets", {
              method: "POST",
              body: toPayload(ticket),
            });
            created++;
          } catch (error) {
            if (!error.message.includes("ticketNo already exists")) {
              throw error;
            }
          }
        }
        showMessage(created ? format("message.seeded", { count: created }) : t("message.seedExists"));
        await reloadData();
      }).catch((error) => showMessage(error.message));
    }

    async function reindexSearch() {
      if (!canReindexSearch.value) {
        showMessage(t("message.noReindexPermission"));
        return;
      }
      await runWithLoading(async () => {
        const count = await api("/travel-api/v1/search/tickets/reindex", { method: "POST" });
        showMessage(format("message.reindexed", { count }));
      }).catch((error) => showMessage(error.message));
    }

    function exportCsv() {
      const rows = [
        [t("csv.ticketNo"), t("csv.employee"), t("csv.department"), t("csv.route"), t("csv.carrier"), t("csv.amount"), t("csv.status"), t("csv.risk")],
        ...filteredTickets.value.map((ticket) => [
          ticket.ticketNo,
          ticket.employeeName,
          ticket.department,
          `${cityText(ticket.departureCity)}-${cityText(ticket.arrivalCity)}`,
          ticket.carrierNo,
          ticket.amount,
          statusText(ticket.status),
          riskText(ticket.riskLevel),
        ]),
      ];
      const csv = rows.map((row) => row.join(",")).join("\n");
      const blob = new Blob([`\uFEFF${csv}`], { type: "text/csv;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = t("csv.fileName");
      link.click();
      URL.revokeObjectURL(url);
      showMessage(t("message.exported"));
    }

    function money(value) {
      return new Intl.NumberFormat(locale.value === "en" ? "en-US" : "zh-CN", {
        style: "currency",
        currency: "CNY",
        maximumFractionDigits: 0,
      }).format(value || 0);
    }

    onMounted(() => {
      if (currentUser.value) {
        reloadData();
      } else {
        redirectToLogin();
      }
    });

    return {
      apiBase,
      activeSection,
      attachmentText,
      canApproveTickets,
      canCreateTicket,
      canDeleteTickets,
      canEditTickets,
      canManageIdentity,
      canReindexSearch,
      currentUser,
      cancelDelete,
      clearFilters,
      confirmRemoveTicket,
      deleteTarget,
      editableStatuses,
      editableTicketTypes,
      editingId,
      exportCsv,
      filters,
      filteredTickets,
      format,
      goPendingPage,
      goRiskPage,
      goTicketPage,
      isDashboard,
      loading,
      logout,
      metrics,
      money,
      openIdentityCenter,
      openIssueTracker,
      openCreateForm,
      pendingPageCount,
      pendingPager,
      pendingRangeText,
      pendingTickets,
      redirectToLogin,
      reindexSearch,
      reloadData,
      reloadRiskEvents,
      resetPendingPageAndReload,
      resetRiskPageAndReload,
      resetAllPagesAndReload,
      resetTicketPageAndReload,
      removeTicket,
      riskPageCount,
      riskPager,
      riskRangeText,
      riskTickets,
      saveTicket,
      seedDemoData,
      sectionTitle,
      setLocale,
      showApprovalModule,
      showRiskModule,
      showTicketForm,
      showTicketModule,
      statuses,
      switchSection,
      ticketForm,
      ticketPageCount,
      ticketPager,
      ticketRangeText,
      ticketTypes,
      toast,
      updateStatus,
      cities,
      cityText,
      editTicket,
      locale,
      sidebarCollapsed,
      statusText,
      t,
      ticketTypeText,
      toggleSidebar,
      riskText,
    };
  },
}).mount("#app");
