SELECT
    team_id as id,
    team_scope as teamScope,
    name as name,
    avatar_ref as avatarId,
    is_active as active,
    created_at as createdAt,
    updated_at as updatedAt
FROM OA_TEAM
WHERE team_id = :teamId
LIMIT 1