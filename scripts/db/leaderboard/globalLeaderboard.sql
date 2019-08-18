<if(hasUser||isTopN||onlyFinalTops||hasPointThreshold)>
SELECT
    *
FROM
(
<endif>

    SELECT
        tbl.user_id AS userId,
        COALESCE(oau.nickname, oau.user_name, oau.email) AS userName,

        tbl.totalPoints AS totalPoints,
        tbl.totalCount AS totalCount,
        (RANK() over (ORDER BY tbl.totalPoints DESC, tbl.totalCount ASC)) AS 'rankGlobal',
        (LAG(tbl.totalPoints) over (ORDER BY tbl.totalPoints DESC, tbl.totalCount ASC)) AS 'nextRankValue',
        (FIRST_VALUE(tbl.totalPoints) over (ORDER BY tbl.totalPoints DESC, tbl.totalCount ASC)) AS 'topRankValue',
        UNIX_TIMESTAMP(NOW()) * 1000 AS calculatedTime
    FROM
    (
        <if(hasStates)>
            SELECT
              user_id,
              SUM(totalCount) AS totalCount,
              ROUND(<aggType>(totalPoints), 2) AS totalPoints
            FROM (
        <endif>

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
                AND point_name IN (<ruleIds>)
                <endif>
                <if(hasExclusions)>
                AND point_name NOT IN (<excludeRuleIds>)
                <endif>
            GROUP BY
                user_id

        <if(hasStates)>
            UNION ALL
                SELECT
                    user_id,
                    COUNT(current_points) AS totalCount,
                    ROUND(<aggType>(current_points), 2) AS totalPoints
                FROM OA_RATING
                WHERE
                    is_active = 1
                    <if(hasTimeRange)>
                    AND changed_at >= :rangeStart
                    AND changed_at \< :rangeEnd
                    <endif>
                GROUP BY
                    user_id
            ) AS groupedPoints
            GROUP BY groupedPoints.user_id
        <endif>

    ) tbl
    INNER JOIN OA_USER oau
        ON oau.user_id = tbl.user_id


<if(hasUser||isTopN||onlyFinalTops||hasPointThreshold)>
) rankTbl

WHERE
    1 = 1
<if(hasUser)>
    AND rankTbl.userId = :userId
<endif>

<if(hasPointThreshold)>
    AND rankTbl.totalPoints >= :pointThreshold
<endif>

<if(onlyFinalTops)>
    AND rankTbl.rankGlobal \<= :topThreshold
<endif>

<if(isTopN)>
ORDER BY rankTbl.rankGlobal ASC
LIMIT :topN
<endif>

<endif>
