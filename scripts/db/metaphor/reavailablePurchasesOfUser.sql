UPDATE OA_SHOP_ITEM oasi
    JOIN OA_PURCHASE oap ON oap.item_id = oasi.id
SET
    oasi.max_available = oasi.max_available + 1
WHERE
    oap.user_id = :userId
    AND
    oasi.limited_amount = 1
    AND
    oap.is_active = 1