UPDATE OA_TEAM_USER
SET
    will_end_at = :endTime
WHERE
    id = :id
    AND
    is_approved = 1
    AND
    until IS NULL