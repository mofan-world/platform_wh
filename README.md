# 统一运营平台

本平台保留两个业务边界：

- `identity-service`：统一身份认证服务，负责登录、刷新令牌、用户、角色、权限，以及组织机构、菜单、岗位、微服务模块和字典管理。
- `issuetracker-end`：问题单跟踪业务服务，负责问题单、项目、版本、附件和流转处理。
- `gateway`：统一入口，承载问题单前端并代理身份认证和问题单 API。
- `postgres`、`redis`、`elasticsearch`、`nacos`：共享基础设施。

## 访问地址

- 统一入口：http://localhost
- 身份认证服务直连：http://localhost:8083
- 问题单跟踪服务直连：http://localhost:8082
- Nacos 控制台：http://localhost:8081

默认管理员：

```text
admin / Admin@123456
```

## 数据与中间件

- PostgreSQL 使用 `platform_identity` 存放身份认证和问题单跟踪数据。
- Redis 使用同一个实例，`identity-service` 使用 DB 0，`issuetracker-end` 使用 DB 1。
- Elasticsearch 用于问题单检索。
- Nacos 用于身份认证服务和问题单服务注册配置。

## API 路由

- `/api/auth/**` -> `identity-service`
- `/api/admin/users/**` -> `identity-service`
- `/api/admin/roles` -> `identity-service`
- `/api/admin/identity/**` -> `identity-service`
- `/api/users/**` -> `identity-service`
- `/api/**` -> `issuetracker-end`

## 身份认证管理

身份认证系统新增以下管理能力，统一由 `identity:manage` 权限控制：

- 组织机构管理
- 菜单管理
- 权限管理
- 角色岗位管理
- 微服务模块管理
- 字典管理

管理员角色 `ADMIN` 会在数据库迁移中自动获得 `identity:manage` 权限。

## 启动

```powershell
Copy-Item .env.example .env

Set-Location services\issue-tracker
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\identity-service
mvn.cmd -s .mvn\settings.xml -DskipTests package

Set-Location ..\..\apps\identity-web
npm.cmd ci
npm.cmd run build

Set-Location ..\..\apps\issue-tracker-web
npm.cmd ci
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

## 验证

- `identity-service` 和 `issuetracker-end` readiness 均为 `UP`。
- `gateway /platform-health` 返回 `200 ok`。
- 通过 `http://localhost/api/auth/login` 登录，实际由 `identity-service` 处理。
- `admin` 登录后包含 `ADMIN` 角色，并具备 `user:manage`、`identity:manage` 和问题单管理权限。
