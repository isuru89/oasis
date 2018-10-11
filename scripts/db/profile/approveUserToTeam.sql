UPDATE OA_TEAM_USER
SET
    is_approved = 1,
    approved_at = :approvedTime
WHERE
    id = :id
    AND
    is_approved = 0
