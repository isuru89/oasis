UPDATE OA_PURCHASE
SET
    is_active = 0
WHERE
    user_id = :userId
    AND
    is_active = 1