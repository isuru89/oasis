SELECT
    hero_id AS heroId,
    display_name AS displayName,
    description AS description,
    is_active AS active

FROM OA_USER_HERO
WHERE
    is_active = 1