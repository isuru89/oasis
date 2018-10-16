<if(hasUser||isTopN||isBottomN)>
SELECT
    *
FROM
(
<endif>

    SELECT
        tbl.user_id AS userId,
        tbl.totalPoints AS totalPoints,
        (RANK() over (ORDER BY tbl.totalPoints DESC)) AS 'rankGlobal',
        (LAG(tbl.totalPoints) over (ORDER BY tbl.totalPoints DESC)) AS 'nextRankVal',
        (FIRST_VALUE(tbl.totalPoints) over (ORDER BY tbl.totalPoints DESC)) AS 'topRankVal',
        UNIX_TIMESTAMP(NOW()) * 1000 AS calculatedTime
    FROM
    (
        SELECT
            user_id,
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
