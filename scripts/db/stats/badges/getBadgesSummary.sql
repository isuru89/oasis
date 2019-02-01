SELECT
    oad.name as badgeName,
    oad.display_name as badgeDisplayName,
   tbl.*

FROM
(
    SELECT
          <if(hasUserId)>
            oab.user_id as userId,
          <elseif(hasTeamId)>
            oab.team_id as teamId,
          <else>
            oab.team_scope_id AS teamScopeId,
          <endif>
        oab.badge_id as badgeId,
        oab.sub_badge_id as subBadgeId,
        oada.attribute_id AS badgeAttribute,
        oaat.display_name AS badgeAttributeName,
        COUNT(oab.badge_id) as badgeCount

    FROM OA_BADGE oab
        LEFT JOIN OA_DEFINITION_ATTR oada
                ON oada.def_id = oab.badge_id AND oada.def_sub_id = oab.sub_badge_id
        LEFT JOIN OA_ATTRIBUTE oaat ON oaat.id = oada.attribute_id

    WHERE
        oab.is_active = 1
        <if(hasUserId)>
            AND
            oab.user_id = :userId
        <elseif(hasTeamId)>
            AND
            oab.team_id = :teamId
        <else>
            AND
            oab.team_scope_id = :teamScopeId
        <endif>

        <if(hasRangeStart)>
            AND oab.ts >= :rangeStart
        <endif>
        <if(hasRangeEnd)>
            AND oab.ts \< :rangeEnd
        <endif>

    GROUP BY
          <if(hasUserId)>
            oab.user_id,
          <elseif(hasTeamId)>
            oab.team_id,
          <else>
            oab.team_scope_id,
          <endif>
        oab.badge_id,
        oab.sub_badge_id

) tbl
INNER JOIN OA_DEFINITION oad ON oad.id = tbl.badgeId

WHERE
    oad.is_active = 1

ORDER BY
    tbl.badgeCount DESC
