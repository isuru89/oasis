SELECT
    user_id as userId,
    'Badges' as typeId,
    COUNT(*) as totalBadges
FROM OA_BADGES
WHERE
    user_id = :userId AND is_active = 1
GROUP BY user_id

UNION ALL

SELECT
    user_id as userId,
    'Last Week Badges' as typeId,
    COUNT(*) as totalPoints
FROM OA_BADGES
WHERE
    user_id = :userId
    AND
    is_active = 1
    AND
    ts >= :startDate
GROUP BY user_id