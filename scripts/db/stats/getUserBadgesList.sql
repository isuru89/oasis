SELECT
    oab.user_id AS userId,
    oab.team_id AS teamId,
    oat.name AS teamName,
    oab.team_scope_id  AS teamScopeId,
    oats.display_name AS teamScopeName,
    oab.event_type AS eventType,
    oab.ext_id AS extId,
    oab.ts AS ts,
    oab.tag AS tag,
    oab.badge_id AS badgeId,
    oab.sub_badge_id AS subBadgeId,
    oab.start_ext_id AS extIdStart,
    oab.end_ext_id AS extIdEnd,
    oab.start_time AS timeStart,
    oab.end_time AS timeEnd

FROM OA_BADGES oab
    LEFT JOIN OA_TEAM oat ON oab.team_id = oat.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON oats.scope_id = oat.team_scope

WHERE
    oab.user_id = :userId
    AND
    oab.is_active = 1
    <if(hasBadgeId)>
    AND oab.badge_id = :badgeId
    <endif>
    <if(hasRangeStart)>
    AND oab.ts >= :rangeStart
    <endif>
    <if(hasRangeEnd)>
    AND oab.ts \< :rangeEnd
    <endif>

ORDER BY oab.ts DESC

<if(hasSize)>
LIMIT
    :size
<if(hasOffset)>
OFFSET
    :offset
<endif>
<endif>