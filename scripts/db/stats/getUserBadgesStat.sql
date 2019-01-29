SELECT
    oab.user_id as userId,
    oab.badge_id as badgeId,
    oab.sub_badge_id as subBadgeId,
    oada.attribute_id AS badgeAttr,
    oaat.display_name AS badgeAttrDisplayName,
    COUNT(*) as badgeCount

FROM OA_BADGE oab
    LEFT JOIN OA_DEFINITION_ATTR oada
            ON oada.def_id = oab.badge_id AND oada.def_sub_id = oab.sub_badge_id
    LEFT JOIN OA_ATTRIBUTE oaat ON oaat.id = oada.attribute_id

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

GROUP BY
    oab.user_id,
    oab.badge_id,
    oab.sub_badge_id

LIMIT :size
OFFSET :offset