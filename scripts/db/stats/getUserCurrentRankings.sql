SELECT
    ranktbl.*
FROM
(
    SELECT
        tbl.user_id AS userId,
        tbl.team_id AS teamId,
        tbl.team_scope_id AS teamScopeId,
        tbl.totalPoints AS totalPoints,
        (RANK() over (PARTITION BY tbl.team_id ORDER BY tbl.totalPoints DESC)) AS 'rankTeam',
        (RANK() over (PARTITION BY tbl.team_scope_id ORDER BY tbl.totalPoints DESC)) AS 'rankTeamScope',
        UNIX_TIMESTAMP(NOW()) * 1000 AS calculatedTime
    FROM
    (
        SELECT
            user_id,
            team_scope_id,
            team_id,
            ROUND(SUM(points), 2) AS totalPoints
        FROM OA_POINTS
        WHERE
            is_active = 1
            AND team_scope_id = :teamScopeId
        GROUP BY
            team_id,
            team_scope_id,
            user_id
    ) tbl

) ranktbl

WHERE
    ranktbl.userId = :userId
    <if(teamWise)>
    AND ranktbl.teamId = :teamId
    <endif>

