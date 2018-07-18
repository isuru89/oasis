SELECT
    ext_id as teamId,
    team_scope as teamScope,
    name as name,
    avatar_id as avatarId,
    created_at as createdAt,
    updated_at as updatedAt
FROM OA_TEAM
WHERE ext_id = :teamId
LIMIT 1