UPDATE OA_PURCHASE
SET
    is_active = 0
WHERE
    item_id = :itemId