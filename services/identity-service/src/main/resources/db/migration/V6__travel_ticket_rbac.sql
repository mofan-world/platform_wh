INSERT INTO permissions(code, name) VALUES
('travel:ticket:read', '查看出差车票'),
('travel:ticket:create', '创建出差车票'),
('travel:ticket:update', '更新出差车票'),
('travel:ticket:delete', '删除出差车票'),
('travel:ticket:approve', '审批出差车票'),
('travel:risk:read', '查看出差车票风险'),
('travel:search:reindex', '重建出差车票搜索索引'),
('travel:ops:read', '查看出差车票操作日志')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO roles(code, name) VALUES
('TRAVEL_USER', '出差车票用户'),
('TRAVEL_APPROVER', '出差车票审批员'),
('TRAVEL_AUDITOR', '出差车票审计员'),
('TRAVEL_ADMIN', '出差车票管理员')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TRAVEL_USER'
  AND p.code IN ('travel:ticket:read', 'travel:ticket:create', 'travel:ticket:update', 'travel:risk:read')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TRAVEL_APPROVER'
  AND p.code IN ('travel:ticket:read', 'travel:ticket:update', 'travel:ticket:approve', 'travel:risk:read')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TRAVEL_AUDITOR'
  AND p.code IN ('travel:ticket:read', 'travel:risk:read', 'travel:ops:read')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TRAVEL_ADMIN'
  AND p.code LIKE 'travel:%'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.deleted = FALSE
  AND r.code = 'TRAVEL_ADMIN'
  AND EXISTS (
      SELECT 1
      FROM user_roles ur
      JOIN roles existing_role ON existing_role.id = ur.role_id
      WHERE ur.user_id = u.id
        AND existing_role.code IN ('ADMIN', 'MANAGER')
  )
ON CONFLICT DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.deleted = FALSE
  AND r.code = 'TRAVEL_APPROVER'
  AND EXISTS (
      SELECT 1
      FROM user_roles ur
      JOIN roles existing_role ON existing_role.id = ur.role_id
      WHERE ur.user_id = u.id
        AND existing_role.code IN ('REVIEWER', 'TESTER')
  )
ON CONFLICT DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.deleted = FALSE
  AND r.code = 'TRAVEL_USER'
  AND EXISTS (
      SELECT 1
      FROM user_roles ur
      JOIN roles existing_role ON existing_role.id = ur.role_id
      WHERE ur.user_id = u.id
        AND existing_role.code IN ('USER', 'AGENT', 'DEVELOPER')
  )
ON CONFLICT DO NOTHING;
