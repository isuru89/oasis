SELECT
    user_id as userId,
    'Points' as typeId,
    SUM(points) as totalPoints
FROM OA_POINTS
WHERE
    user_id = :userId
    AND
    is_active = 1
GROUP BY user_id

UNION ALL

SELECT
    user_id as userId,
    'Last Week Points' as typeId,
    SUM(points) as totalPoints
FROM OA_POINTS
WHERE
    user_id = :userId
    AND
    is_active = 1
    AND
    ts >= :startDate
GROUP BY user_id