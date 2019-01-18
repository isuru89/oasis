UPDATE OA_TEAM_USER
SET
    is_approved = 1,
    approved_at = :approvedTime,
    until = will_end_at
WHERE
    id = :id
    AND
    is_approved = 0
