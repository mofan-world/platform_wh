UPDATE menus
SET name = '系统配置',
    component = NULL,
    icon = 'Setting',
    permission_code = 'identity:manage',
    sort_order = 20,
    visible = TRUE,
    enabled = TRUE
WHERE path = '/admin/identity';

INSERT INTO menus(parent_id, module_id, name, path, component, icon, permission_code, sort_order, visible, enabled)
SELECT parent.id, module.id, item.name, item.path, 'IdentityManagementView', item.icon, 'identity:manage', item.sort_order, TRUE, TRUE
FROM service_modules module
JOIN menus parent ON parent.path = '/admin/identity'
CROSS JOIN (
    VALUES
        ('组织机构', '/admin/identity/organizations', 'OfficeBuilding', 10),
        ('菜单管理', '/admin/identity/menus', 'Menu', 20),
        ('权限管理', '/admin/identity/permissions', 'Key', 30),
        ('角色岗位', '/admin/identity/roles-posts', 'UserFilled', 40),
        ('微服务模块', '/admin/identity/modules', 'Grid', 50),
        ('字典管理', '/admin/identity/dictionaries', 'Collection', 60)
) AS item(name, path, icon, sort_order)
WHERE module.code = 'IDENTITY'
  AND NOT EXISTS (
      SELECT 1
      FROM menus existing
      WHERE existing.path = item.path
  );

UPDATE menus
SET module_id = module.id,
    icon = 'User',
    sort_order = 10,
    visible = TRUE,
    enabled = TRUE
FROM service_modules module
WHERE module.code = 'IDENTITY'
  AND menus.path = '/admin/users';

INSERT INTO menus(module_id, name, path, component, icon, permission_code, sort_order, visible, enabled)
SELECT module.id, item.name, item.path, item.component, item.icon, item.permission_code, item.sort_order, TRUE, TRUE
FROM service_modules module
CROSS JOIN (
    VALUES
        ('问题单列表', '/tickets', 'TicketListView', 'Tickets', 'ticket:read:own|ticket:read:all', 10),
        ('创建问题单', '/tickets/new', 'TicketCreateView', 'Plus', 'ticket:create', 20),
        ('版本管理', '/admin/versions', 'VersionManagementView', 'Collection', 'version:manage', 30),
        ('项目管理', '/admin/projects', 'ProjectManagementView', 'FolderOpened', 'project:manage', 40)
) AS item(name, path, component, icon, permission_code, sort_order)
WHERE module.code = 'ISSUE_TRACKER'
  AND NOT EXISTS (
      SELECT 1
      FROM menus existing
      WHERE existing.path = item.path
  );
