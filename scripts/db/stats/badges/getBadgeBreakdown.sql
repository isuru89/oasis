SELECT
    oab.user_id AS userId,
    <if(!hasUserId)>
        COALESCE(oau.nickname, oau.user_name, oau.email) AS userName,
    <endif>
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
    oada.attribute_id AS badgeAttr,
    oaat.display_name AS badgeAttrDisplayName,
    oab.start_ext_id AS extIdStart,
    oab.end_ext_id AS extIdEnd,
    oab.start_time AS timeStart,
    oab.end_time AS timeEnd

FROM OA_BADGE oab
    LEFT JOIN OA_TEAM oat ON oab.team_id = oat.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON oats.scope_id = oat.team_scope
    LEFT JOIN OA_DEFINITION_ATTR oada
            ON oada.def_id = oab.badge_id AND oada.def_sub_id = oab.sub_badge_id
    LEFT JOIN OA_ATTRIBUTE oaat ON oaat.id = oada.attribute_id
    <if(!hasUserId)>
        LEFT JOIN OA_USER oau ON oau.user_id = oab.user_id
    <endif>

WHERE
    oab.is_active = 1
    <if(hasUserId)>
        AND oab.user_id = :userId
    <endif>
    <if(hasTeamId)>
        AND oab.team_id = :teamId
    <endif>
    <if(hasTeamScopeId)>
        AND oab.team_scope_id = :teamScopeId
    <endif>

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