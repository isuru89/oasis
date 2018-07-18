SELECT
    id as id,
    ext_id as userId,
    event_type as eventType,
    ext_id as externalId,
    ts as achievedTime,
    badge_id as badgeId,
    sub_badge_id as subBadgeId,
    start_ext_id as startExtId,
    end_ext_id as endExtId,
    start_time as startTime,
    end_time as endTime,
    tag as tag

FROM OA_BADGES bdg
    INNER JOIN OA_DEFINITION odef ON bdg.badge_id = odef.id
WHERE
    bdg.ext_id = :userId
    AND
    odef.is_active = 1