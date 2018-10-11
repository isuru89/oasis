UPDATE OA_TEAM_USER
SET
    until = :endTime
WHERE
    id = :id
    AND
    is_approved = 1