INSERT INTO OA_TEAM_SCOPE_USER (
    team_scope_id,
    user_id,
    role_id,
    since,
    is_approved,
    approved_at
) VALUES (
    :teamScopeId,
    :userId,
    :roleId,
    :startTime,
    0,
    null
)