UPDATE OA_PURCHASE
SET
    share_at = :currentEpoch,
    is_active = 0
WHERE
    item_id = :itemId
    AND
    user_id = :userId
    AND
    via_friend = 0
LIMIT
    :amount