SELECT
    team_id as id,
    team_scope as teamScope,
    name as name,
    avatar_ref as avatarId,
    is_auto_team as autoTeam,
    is_active as active,
    created_at as createdAt,
    updated_at as updatedAt
FROM OA_TEAM
WHERE LOWER(name) = LOWER(:teamName) AND is_active = 1
LIMIT 1