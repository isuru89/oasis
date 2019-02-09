SELECT
    item_id AS itemId,
    user_id AS userId,
    cost AS cost,
    purchased_at AS purchasedAt,
    shared_at AS sharedAt,
    via_friend AS viaFriend

FROM OA_PURCHASE
WHERE
    user_id = :userId
    AND
    is_active = 1