UPDATE OA_TEAM_SCOPE_USER
SET
    is_approved = 1,
    approved_at = :approvedAt,
    since = :approvedAt,
    modified_by = :modifiedBy

WHERE
    id = :id
    AND
    is_approved = 0