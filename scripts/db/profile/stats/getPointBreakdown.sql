SELECT
    oad.name as pointName,
    oad.displayName as pointDisplayName,
    tbl.*

FROM (
    SELECT
        oap.user_id as userId,
        oap.point_id as pointId,
        ROUND(SUM(oap.points), 2) as totalPoints,
        COUNT(*) as occurrences,
        MIN(oap.points) as minAchieved,
        MAX(oap.points) as maxAchieved,
        MIN(oap.ts) as firstAchieved,
        MAX(oap.ts) as lastAchieved

    FROM OA_POINTS oap
    WHERE
        oap.user_id = :userId
        AND
        oap.is_active = 1

    GROUP BY
        oap.user_id,
        oap.point_id

    ) tbl
    INNER JOIN OA_DEFINITION oad ON tbl.pointId = oad.id

WHERE
    oad.is_active = 1
