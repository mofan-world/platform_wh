CREATE TABLE service_modules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    route_prefix VARCHAR(120),
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS module_id BIGINT REFERENCES service_modules(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'DEPARTMENT',
    sort_order INTEGER NOT NULL DEFAULT 0,
    leader VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE menus (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES menus(id) ON DELETE SET NULL,
    module_id BIGINT REFERENCES service_modules(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    permission_code VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dictionary_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dictionary_items (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL REFERENCES dictionary_types(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    remark TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dictionary_items_type_value UNIQUE (type_id, value)
);

INSERT INTO service_modules(code, name, route_prefix, description, sort_order)
VALUES
('IDENTITY', '身份认证系统', '/api/admin/identity', '用户、组织、角色、权限和系统基础配置', 10),
('ISSUE_TRACKER', '问题单跟踪系统', '/api', '问题单创建、跟踪、处理和项目协作', 20)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    route_prefix = EXCLUDED.route_prefix,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order;

INSERT INTO permissions(code, name, description, module_id, sort_order)
SELECT 'identity:manage', '管理身份认证配置', '组织机构、菜单、权限、角色岗位、服务模块和字典管理',
       m.id, 10
FROM service_modules m
WHERE m.code = 'IDENTITY'
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    module_id = EXCLUDED.module_id,
    sort_order = EXCLUDED.sort_order,
    enabled = TRUE;

UPDATE permissions p
SET module_id = m.id
FROM service_modules m
WHERE m.code = 'IDENTITY'
  AND p.code IN ('user:manage', 'identity:manage');

UPDATE permissions p
SET module_id = m.id
FROM service_modules m
WHERE m.code = 'ISSUE_TRACKER'
  AND (
      p.code LIKE 'ticket:%'
      OR p.code LIKE 'version:%'
      OR p.code LIKE 'project:%'
      OR p.code LIKE 'attachment:%'
  );

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
  AND p.code = 'identity:manage'
ON CONFLICT DO NOTHING;

INSERT INTO organizations(code, name, type, sort_order, description)
VALUES ('ROOT', '总部', 'COMPANY', 0, '默认组织机构')
ON CONFLICT (code) DO NOTHING;

INSERT INTO posts(code, name, sort_order, description)
VALUES
('ADMIN', '系统管理员', 10, '平台身份认证和系统管理岗位'),
('USER', '普通用户', 20, '默认业务用户岗位')
ON CONFLICT (code) DO NOTHING;

INSERT INTO menus(module_id, name, path, component, icon, permission_code, sort_order)
SELECT m.id, '用户与权限', '/admin/users', 'UserManagementView', 'User', 'user:manage', 10
FROM service_modules m
WHERE m.code = 'IDENTITY'
ON CONFLICT DO NOTHING;

INSERT INTO menus(module_id, name, path, component, icon, permission_code, sort_order)
SELECT m.id, '身份配置', '/admin/identity', 'IdentityManagementView', 'Setting', 'identity:manage', 20
FROM service_modules m
WHERE m.code = 'IDENTITY'
ON CONFLICT DO NOTHING;

INSERT INTO dictionary_types(code, name, description)
VALUES
('ORG_TYPE', '组织类型', '组织机构类型字典'),
('MENU_VISIBLE', '菜单可见性', '菜单显示状态字典')
ON CONFLICT (code) DO NOTHING;

INSERT INTO dictionary_items(type_id, label, value, sort_order)
SELECT t.id, '公司', 'COMPANY', 10 FROM dictionary_types t WHERE t.code = 'ORG_TYPE'
ON CONFLICT DO NOTHING;

INSERT INTO dictionary_items(type_id, label, value, sort_order)
SELECT t.id, '部门', 'DEPARTMENT', 20 FROM dictionary_types t WHERE t.code = 'ORG_TYPE'
ON CONFLICT DO NOTHING;

DELETE FROM user_roles ur
USING roles r
WHERE ur.role_id = r.id
  AND r.code LIKE 'TRAVEL_%';

DELETE FROM role_permissions rp
USING roles r
WHERE rp.role_id = r.id
  AND r.code LIKE 'TRAVEL_%';

DELETE FROM role_permissions rp
USING permissions p
WHERE rp.permission_id = p.id
  AND p.code LIKE 'travel:%';

DELETE FROM roles
WHERE code LIKE 'TRAVEL_%';

DELETE FROM permissions
WHERE code LIKE 'travel:%';

CREATE INDEX idx_organizations_parent_sort ON organizations(parent_id, sort_order, id);
CREATE INDEX idx_posts_enabled_sort ON posts(enabled, sort_order, id);
CREATE INDEX idx_service_modules_enabled_sort ON service_modules(enabled, sort_order, id);
CREATE INDEX idx_menus_module_parent_sort ON menus(module_id, parent_id, sort_order, id);
CREATE INDEX idx_permissions_module_sort ON permissions(module_id, sort_order, id);
CREATE INDEX idx_dictionary_types_enabled_code ON dictionary_types(enabled, code);
CREATE INDEX idx_dictionary_items_type_sort ON dictionary_items(type_id, sort_order, id);
