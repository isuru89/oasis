<if(hasUser||isTopN||isBottomN)>
SELECT
    *
FROM
(
<endif>

    SELECT
        tbl.user_id AS userId,
        tbl.totalPoints AS totalPoints,
        tbl.totalCount AS totalCount,
        (RANK() over (w)) AS 'rankGlobal',
        (LAG(tbl.totalPoints) over (w)) AS 'nextRankVal',
        (FIRST_VALUE(tbl.totalPoints) over (w)) AS 'topRankVal',
        UNIX_TIMESTAMP(NOW()) * 1000 AS calculatedTime
    FROM
    (
        SELECT
            user_id,
            COUNT(points) AS totalCount,
            ROUND(<aggType>(points), 2) AS totalPoints
        FROM OA_POINT
        WHERE
            is_active = 1
            <if(hasTimeRange)>
            AND ts >= :rangeStart
            AND ts \< :rangeEnd
            <endif>

            <if(hasInclusions)>
            AND point_id IN \<ruleIds>
            <endif>
        GROUP BY
            user_id
    ) tbl
    WINDOW w AS (ORDER BY tbl.totalPoints DESC, tbl.totalCount ASC)

<if(hasUser||isTopN||isBottomN)>
) rankTbl

<if(hasUser)>
WHERE
    rankTbl.userId = :userId
<endif>

<if(isTopN)>
ORDER BY rankTbl.rankGlobal ASC
LIMIT :topN
<endif>

<if(isBottomN)>
ORDER BY rankTbl.rankGlobal DESC
LIMIT :bottomN
<endif>

<endif>
