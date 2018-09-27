UPDATE OA_USER
SET last_logout_at = :logoutAt
WHERE
    user_id = :userId
    AND
    is_active = 1