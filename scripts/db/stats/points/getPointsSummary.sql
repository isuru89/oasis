SELECT
    oad.name as pointName,
    oad.display_name as pointDisplayName,
    tbl.*

FROM (
    SELECT
      <if(hasUserId)>
        oap.user_id as userId,
      <elseif(hasTeamId)>
        oap.team_id as teamId,
      <else>
        oap.team_scope_id AS teamScopeId,
      <endif>
        oap.point_id as pointId,
        ROUND(SUM(oap.points), 2) as totalPoints,
        COUNT(oap.points) as occurrences,
        MIN(oap.points) as minAchieved,
        MAX(oap.points) as maxAchieved,
        MIN(oap.ts) as firstAchieved,
        MAX(oap.ts) as lastAchieved

    FROM OA_POINT oap
    WHERE
        oap.is_active = 1
        <if(hasUserId)>
            AND
            oap.user_id = :userId
        <elseif(hasTeamId)>
            AND
            oap.team_id = :teamId
        <else>
            AND
            oap.team_scope_id = :teamScopeId
        <endif>

        <if(hasRangeStart)>
            AND
            oap.ts >= :rangeStart
        <endif>
        <if(hasRangeEnd)>
            AND
            oap.ts \< :rangeEnd
        <endif>

    GROUP BY
      <if(hasUserId)>
        oap.user_id,
      <elseif(hasTeamId)>
        oap.team_id,
      <else>
        oap.team_scope_id,
      <endif>
        oap.point_id

    ) tbl
    INNER JOIN OA_DEFINITION oad ON tbl.pointId = oad.id

WHERE
    oad.is_active = 1

ORDER BY
    tbl.totalPoints DESC, tbl.occurrences ASC
