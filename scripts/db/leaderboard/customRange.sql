<if(hasUser)>
SELECT
    *
FROM (
<endif>

SELECT
    tbl.user_id AS userId,
    <if(teamWise)>tbl.team_id AS teamId,<endif>
    <if(teamScopeWise)>tbl.team_scope_id AS teamScopeId,<endif>
    tbl.totalPoints AS totalPoints,
    (RANK() over (PARTITION BY tbl.timeScope ORDER BY tbl.totalPoints DESC)) AS 'rank'

FROM
    (
        SELECT
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
            <if(teamWise)>team_id,<endif>
            <if(teamScopeWise)>team_scope_id,<endif>
            user_id
    ) tbl

<if(hasUser)>
) t

WHERE
    t.userId = :userId

<endif>