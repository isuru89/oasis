SELECT
    scope_id as id,
    ext_id as extId,
    name as name,
    display_name as displayName,
    is_auto_scope as autoScope,
    is_active as active,
    created_at as createdAt,
    updated_at as updatedAt
FROM OA_TEAM_SCOPE
WHERE scope_id = :scopeId
LIMIT 1