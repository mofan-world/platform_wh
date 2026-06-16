UPDATE service_modules
SET name = '系统管理',
    description = '用户、组织、角色、权限和系统基础配置'
WHERE code = 'IDENTITY';

UPDATE permissions
SET name = '管理系统配置',
    description = '组织机构、菜单、权限、角色岗位、服务模块和字典管理'
WHERE code = 'identity:manage';

UPDATE posts
SET description = '平台系统管理岗位'
WHERE code = 'ADMIN';

UPDATE menus
SET name = '系统配置'
WHERE path = '/admin/identity'
  AND permission_code = 'identity:manage';
