SELECT
    oap.user_id AS userId,
    <if(!hasUserId)>
        COALESCE(oau.nickname, oau.user_name, oau.email) AS userName,
    <endif>
    oap.team_id AS teamId,
    oat.name AS teamName,
    oap.team_scope_id AS teamScopeId,
    oats.display_name AS teamScopeName,
    oap.event_type as eventType,
    oap.ext_id as extId,
    oap.ts as ts,
    oap.point_id as pointId,
    oap.point_name as pointName,
    oap.points as points,
    oap.tag as tag

FROM OA_POINT oap
    LEFT JOIN OA_TEAM oat ON oap.team_id = oat.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON oats.scope_id = oat.team_scope
    <if(!hasUserId)>
        LEFT JOIN OA_USER oau ON oau.user_id = oap.user_id
    <endif>

WHERE
    oap.is_active = 1
    <if(hasUserId)>
        AND
        oap.user_id = :userId
    <endif>
    <if(hasTeamId)>
        AND
        oap.team_id = :teamId
    <endif>
    <if(hasTeamScopeId)>
        AND
        oap.team_scope_id = :teamScopeId
    <endif>
    <if(hasPointId)>
        AND
        oap.point_id = :pointId
    <endif>
    <if(hasRangeStart)>
        AND
        oap.ts >= :rangeStart
    <endif>
    <if(hasRangeEnd)>
        AND
        oap.ts \< :rangeEnd
    <endif>

ORDER BY oap.ts DESC

<if(hasSize)>
LIMIT
    :size
<if(hasOffset)>
OFFSET
    :offset
<endif>
<endif>
