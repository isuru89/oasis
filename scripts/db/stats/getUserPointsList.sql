SELECT
    oap.user_id AS userId,
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

WHERE
    oap.user_id = :userId
    AND
    oap.point_id = :pointId
    AND
    <if(hasRangeStart)>
    oap.ts >= rangeStart AND
    <endif>
    <if(hasRangeEnd)>
    oap.ts \< rangeEnd AND
    <endif>
    oap.is_active = 1

ORDER BY oap.ts DESC

<if(hasSize)>
LIMIT
    :size
<if(hasOffset)>
OFFSET
    :offset
<endif>
<endif>
