UPDATE OA_TEAM_SCOPE_USER
SET
    is_approved = 0,
    until = -1,
    modified_by = :modifiedBy

WHERE
    id = :id
    AND
    is_approved = 0
    AND
    modified_by IS NULL