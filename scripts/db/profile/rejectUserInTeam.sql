UPDATE OA_TEAM_USER
SET
    until = -1,
    is_approved = 0
WHERE
    id = :id