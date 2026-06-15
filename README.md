# 统一运营平台

本平台整合 `issuetracker` 与 `出差车票管理系统`，并保持微服务边界：

- `identity-service`：统一身份认证服务，负责登录、刷新令牌、用户、角色、权限。
- `issuetracker-end`：问题跟踪业务微服务。
- `travel-ticket`：出差车票业务微服务，使用 RBAC 校验 JWT 中的角色。
- `gateway`：统一入口，承载两个前端并代理 API。
- `postgres`、`redis`、`elasticsearch`、`nacos`：统一共享基础设施。

## 访问地址

- 统一入口：http://localhost
- 问题跟踪系统：http://localhost/
- 出差车票系统：http://localhost/travel/
- 身份认证服务直连：http://localhost:8083
- 问题跟踪服务直连：http://localhost:8082
- 出差车票服务直连：http://localhost:8090
- Nacos 控制台：http://localhost:8081

默认管理员：

```text
admin / Admin@123456
```

## 数据与中间件划分

- PostgreSQL 使用同一个实例，`platform_identity` 存放身份和问题跟踪数据，`platform_travel` 存放车票业务数据。
- Redis 使用同一个实例，`identity-service` 使用 DB 0，`issuetracker-end` 使用 DB 1，`travel-ticket` 使用 DB 2。
- Elasticsearch 使用同一个实例，各服务使用自己的索引。
- Nacos 使用同一个实例，三个后端微服务共同注册。

## API 路由

- `/api/auth/**` -> `identity-service`
- `/api/admin/users/**` -> `identity-service`
- `/api/admin/roles` -> `identity-service`
- `/api/users/**` -> `identity-service`
- `/api/**` -> `issuetracker-end`
- `/travel-api/**` -> `travel-ticket`

## 出差车票 RBAC

统一身份库新增车票角色：

- `TRAVEL_USER`：查看、创建、编辑车票。
- `TRAVEL_APPROVER`：查看、编辑、审批车票。
- `TRAVEL_AUDITOR`：查看车票、风险和审计日志。
- `TRAVEL_ADMIN`：车票系统管理员，拥有全部车票权限。

`travel-ticket` 会校验 JWT 的 `roles` claim。管理员 `ADMIN` 也被允许执行车票管理员操作，便于平台初始管理。

## 启动

```powershell
Copy-Item .env.example .env

Set-Location services\issue-tracker
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\identity-service
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\travel-ticket
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\..\apps\issue-tracker-web
npm.cmd ci
npm.cmd run build

Set-Location ..\travel-ticket-web
npm.cmd run build

Set-Location ..\..
docker compose up -d --build
```

查看状态：

```powershell
docker compose ps
```

停止：

```powershell
docker compose down
```

## 已验证

- `identity-service`、`issuetracker-end`、`travel-ticket` readiness 均为 `UP`。
- `gateway /platform-health` 返回 `200 ok`。
- 通过 `http://localhost/api/auth/login` 登录，实际由 `identity-service` 处理。
- `admin` 登录后包含 `ADMIN,TRAVEL_ADMIN` 角色和 `travel:*` 权限。
- 使用管理员 JWT 调用 `POST /travel-api/v1/tickets` 可以创建车票。
- 仅有 `TRAVEL_AUDITOR` 角色的用户创建车票返回 `403`。
- 不带 token 访问车票 API 返回 `401`。

内置浏览器可视化验证仍受当前桌面沙箱权限限制，但 HTTP/API 端到端验收已通过。
