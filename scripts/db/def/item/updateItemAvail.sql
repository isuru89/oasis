UPDATE OA_SHOP_ITEM
SET
    max_available = max_available - 1
WHERE
    id = :itemId
    AND
    (max_available > 0 OR max_available = -1)
    AND
    expiration_at > :ts
LIMIT
    1