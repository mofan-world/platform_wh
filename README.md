# 统一运营平台

本平台整合了 `issuetracker` 和 `出差车票管理系统` 两个项目。两个业务系统保持独立微服务运行，通过统一网关暴露入口，并共享同一组 PostgreSQL、Redis、Elasticsearch 和 Nacos。

## 架构

- `issuetracker-end`：问题跟踪与统一身份认证服务，负责登录、刷新令牌、用户角色和权限。
- `travel-ticket`：出差车票业务服务，作为 OAuth2 Resource Server 校验 `issuetracker-end` 签发的 JWT。
- `gateway`：统一入口，提供两个前端页面并代理 API。
- `postgres`：同一个 PostgreSQL 实例，使用 `platform_identity` 和 `platform_travel` 两个数据库隔离迁移脚本和业务表。
- `redis`：同一个 Redis 实例，`issuetracker` 使用 DB 1，车票服务使用 DB 2。
- `elasticsearch`：同一个 ES 实例，两个服务使用各自索引。
- `nacos`：同一个 Nacos 实例，两个微服务共同注册。

## 访问地址

- 统一入口：http://localhost
- 问题跟踪系统：http://localhost/
- 出差车票管理系统：http://localhost/travel/
- Nacos 控制台：http://localhost:8081
- `issuetracker` 后端直连：http://localhost:8082
- 车票后端直连：http://localhost:8090

默认管理员账号：

```text
username: admin
password: Admin@123456
```

## 启动

复制环境变量模板，并按需修改密钥和密码：

```powershell
Copy-Item .env.example .env
```

构建后端 jar：

```powershell
Set-Location services\issue-tracker
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\travel-ticket
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\..
```

构建前端产物：

```powershell
Set-Location apps\issue-tracker-web
npm.cmd ci
npm.cmd run build

Set-Location ..\travel-ticket-web
npm.cmd run build

Set-Location ..\..
```

启动平台：

```powershell
docker compose up -d --build
```

查看状态：

```powershell
docker compose ps
```

停止平台：

```powershell
docker compose down
```

## 认证与授权

统一登录由 `issuetracker-end` 提供：

```http
POST /api/auth/login
```

登录成功后前端保存同一个 `accessToken`。访问车票模块时，网关将 `/travel-api/**` 转发到 `travel-ticket` 微服务，车票服务校验同一个 JWT。

车票服务不会信任浏览器传入的 `X-Tenant-Id`。它会从 JWT 的 `uid` claim 派生租户 ID，并覆盖请求头供业务代码使用。

## 已验证

- `docker compose` 运行 PostgreSQL、Redis、Elasticsearch、Nacos、两个后端微服务和统一网关。
- `issuetracker-end` readiness：`UP`。
- `travel-ticket` readiness：`UP`。
- 统一入口 `/platform-health`：`200 ok`。
- 通过 `http://localhost/api/auth/login` 登录后，使用同一个 JWT 调用 `http://localhost/travel-api/v1/reports/summary` 成功。
- 未携带 token 调用车票 API 返回 `401`。

内置浏览器可视化验证在当前桌面沙箱中被系统权限阻止，但 HTTP/API 端到端验证已通过。
# platform_wh
"# platform_wh" 
