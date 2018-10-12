UPDATE OA_TEAM_SCOPE_USER
SET
    until = :endTime,
    modified_by = :modifiedBy

WHERE
    user_id = :userId
    AND
    team_scope_id = :teamScopeId
    AND
    is_approved = 1
    AND
    until IS NULL