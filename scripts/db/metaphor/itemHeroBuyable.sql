SELECT
    *
FROM OA_SHOP_ITEM
WHERE
    id = :itemId
    AND
    is_active = 1
    AND
    (for_hero = 0
     OR
     for_hero & :userHero = :userHero)