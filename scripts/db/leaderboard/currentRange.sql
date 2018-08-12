<if(topN||bottomN||hasUser)>
SELECT
    *
FROM (
<endif>

SELECT
    tbl.user_id AS userId,
    <if(teamWise)>tbl.team_id AS teamId,<endif>
    <if(teamScopeWise)>tbl.team_scope_id AS teamScopeId,<endif>
    tbl.timeScope AS timeScope,
    tbl.totalPoints AS totalPoints,
    (RANK() over (PARTITION BY tbl.timeScope ORDER BY tbl.totalPoints DESC)) AS 'rank'

FROM
    (
        SELECT
            FROM_UNIXTIME(ts / 1000, :timePattern) AS timeScope,
            user_id,
            <if(teamWise)>team_id,<endif>
            <if(teamScopeWise)>team_scope_id,<endif>
            ROUND(SUM(points), 2) AS totalPoints
        FROM
            OA_POINTS
        WHERE
            ts >= :startRange
            AND
            ts \< :endRange
            AND
            is_active = 1
        GROUP BY
            timeScope,
            <if(teamWise)>team_id,<endif>
            <if(teamScopeWise)>team_scope_id,<endif>
            user_id
    ) tbl

<if(topN||bottomN||hasUser)>
) t

<if(hasUser)>
WHERE
    t.userId = :userId
<endif>

<if(bottomN)>
ORDER BY t.rank DESC

LIMIT :bottomN
<endif>

<if(topN)>
LIMIT :topN
<endif>

<endif>