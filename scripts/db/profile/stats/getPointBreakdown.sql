SELECT
    user_id as userId,
    point_id as pointId,
    sub_point_id as subPointId,
    ROUND(SUM(points), 2) as totalPoints

FROM OA_POINTS
WHERE
    user_id = :userId
    AND
    is_active = 1
GROUP BY
    user_id,
    point_id,
    sub_point_id