SELECT
    id AS id,
    team_scope_id AS teamScopeId,
    user_id AS userId,
    role_id AS userRole,
    since AS since,
    until AS until,
    is_approved AS approved,
    approved_at AS approvedAt

FROM OA_TEAM_SCOPE_USER
WHERE
    user_id = :userId
    AND
    is_approved = 1
    AND
    until IS NULL