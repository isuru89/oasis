<if(hasTeam||hasUser)>
SELECT
    *
FROM
(
<endif>

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
            AND
            team_scope_id = :teamScopeId
            <if(hasTimeRange)>
            AND ts >= :rangeStart
            AND ts \< :rangeEnd
            <endif>

            <if(hasInclusions)>
            AND point_id IN \<ruleIds>
            <endif>
            <if(hasExclusions)>
            AND point_id NOT IN \<excludeRuleIds>
            <endif>
        GROUP BY
            team_id,
            team_scope_id,
            user_id
    ) tbl

<if(hasTeam||hasUser)>
) rankTbl

WHERE
    1 = 1
<if(hasTeam)>
    AND rankTbl.teamId = :teamId
<endif>
<if(hasUser)>
    AND rankTbl.userId = :userId
<endif>

<endif>
