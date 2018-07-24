SELECT
    oap.user_id as userId,
    oap.team_id as teamId,
    ROUND(SUM(oap.points), 2) as totalPoints

FROM OA_POINTS oap
WHERE
    oap.user_id = :userId
    AND
    oap.is_active = 1
GROUP BY
    oap.user_id,
    oap.team_id