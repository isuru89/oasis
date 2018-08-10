SELECT
    user_id as userId,
    'TotalPoints' AS type,
    COALESCE(ROUND(SUM(points), 2), 0) as value_f,
    NULL as value_i
FROM OA_POINTS
WHERE user_id = :userId AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'TotalBadges' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_BADGES
WHERE user_id = :userId AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'TotalTrophies' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_CHALLENGE_WINNER
WHERE user_id = :userId AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'TotalItems' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_PURCHASE
WHERE user_id = :userId AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'AmountSpent' AS type,
    COALESCE(ROUND(SUM(cost), 2), 0) as value_f,
    NULL AS value_i
FROM OA_PURCHASE
WHERE user_id = :userId AND is_active = 1

<if(hasSince)>

UNION ALL

SELECT
    user_id as userId,
    'DeltaPoints' AS type,
    COALESCE(ROUND(SUM(points), 2), 0) as value_f,
    NULL as value_i
FROM OA_POINTS
WHERE user_id = :userId AND ts >= :since AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'DeltaBadges' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_BADGES
WHERE user_id = :userId AND ts >= :since AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'DeltaTrophies' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_CHALLENGE_WINNER
WHERE user_id = :userId AND won_at >= :since AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'DeltaItems' AS type,
    NULL as value_f,
    COUNT(*) AS value_i
FROM OA_PURCHASE
WHERE user_id = :userId AND purchased_at >= :since AND is_active = 1

UNION ALL

SELECT
    user_id as userId,
    'DeltaAmountSpent' AS type,
    COALESCE(ROUND(SUM(cost), 2), 0) as value_f,
    NULL AS value_i
FROM OA_PURCHASE
WHERE user_id = :userId AND purchased_at >= :since AND is_active = 1

<endif>