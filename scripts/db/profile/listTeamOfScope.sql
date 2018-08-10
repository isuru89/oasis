SELECT
    oat.team_id as id,
    oat.team_scope as teamScope,
    oat.name as name,
    oat.avatar_id as avatarId,
    oat.created_at as createdAt,
    oat.updated_at as updatedAt

FROM OA_TEAM oat
    LEFT JOIN OA_TEAM_SCOPE oats ON oat.team_scope = oats.scope_id
WHERE
    oats.scope_id = :scopeId
    AND
    oat.is_active = 1
    AND
    oats.is_active = 1