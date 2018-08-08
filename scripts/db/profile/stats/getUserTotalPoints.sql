SELECT
    user_id as userId,
    ROUND(SUM(points), 2) as totalPoints
FROM OA_POINTS
WHERE
    user_id = :userId
    AND
    is_active = 1
GROUP BY user_id