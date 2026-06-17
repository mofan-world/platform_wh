ALTER TABLE users
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS post_id BIGINT REFERENCES posts(id) ON DELETE SET NULL;

UPDATE users
SET organization_id = COALESCE(
    organization_id,
    (SELECT id FROM organizations WHERE code = 'ROOT' ORDER BY id LIMIT 1)
)
WHERE organization_id IS NULL;

UPDATE users
SET post_id = COALESCE(
    post_id,
    (SELECT id FROM posts WHERE code = 'USER' ORDER BY id LIMIT 1)
)
WHERE post_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_organization ON users(organization_id);
CREATE INDEX IF NOT EXISTS idx_users_post ON users(post_id);
