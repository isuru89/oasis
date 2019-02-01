SELECT
   tbl.*,
   COALESCE(oau.nickname, oau.user_name, oau.email) AS userName,
   oau.email AS userEmail

FROM
(

    SELECT
        COALESCE(user_id, :userId) as userId,
        'TotalPoints' AS type,
        COALESCE(ROUND(SUM(points), 2), 0.0) as value_f,
        NULL as value_i
    FROM OA_POINT
    WHERE user_id = :userId AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'TotalBadges' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_BADGE
    WHERE user_id = :userId AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'TotalTrophies' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_CHALLENGE_WINNER
    WHERE user_id = :userId AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'TotalItems' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_PURCHASE
    WHERE user_id = :userId AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'AmountSpent' AS type,
        COALESCE(ROUND(SUM(cost), 2), 0.0) as value_f,
        NULL AS value_i
    FROM OA_PURCHASE
    WHERE user_id = :userId AND is_active = 1

    <if(hasSince)>

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'DeltaPoints' AS type,
        COALESCE(ROUND(SUM(points), 2), 0.0) as value_f,
        NULL as value_i
    FROM OA_POINT
    WHERE user_id = :userId AND ts >= :since AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'DeltaBadges' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_BADGE
    WHERE user_id = :userId AND ts >= :since AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'DeltaTrophies' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_CHALLENGE_WINNER
    WHERE user_id = :userId AND won_at >= :since AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'DeltaItems' AS type,
        NULL as value_f,
        COUNT(*) AS value_i
    FROM OA_PURCHASE
    WHERE user_id = :userId AND purchased_at >= :since AND is_active = 1

    UNION ALL

    SELECT
        COALESCE(user_id, :userId) as userId,
        'DeltaAmountSpent' AS type,
        COALESCE(ROUND(SUM(cost), 2), 0.0) as value_f,
        NULL AS value_i
    FROM OA_PURCHASE
    WHERE user_id = :userId AND purchased_at >= :since AND is_active = 1

    <endif>

) tbl
LEFT JOIN OA_USER oau ON oau.user_id = tbl.userId

