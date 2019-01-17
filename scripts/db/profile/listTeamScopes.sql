SELECT
    scope_id as id,
    ext_id as extId,
    name as name,
    display_name as displayName,
    is_auto_scope as autoScope
FROM OA_TEAM_SCOPE
WHERE is_active = 1