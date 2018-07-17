SELECT
    ID as id,
    USER_ID as userId,
    EVENT_TYPE as eventType,
    EXT_ID as externalId,
    TS as achievedTime,
    BADGE_ID as badgeId,
    SUB_BADGE_ID as subBadgeId,
    START_EXT_ID as startExtId,
    END_EXT_ID as endExtId,
    START_TIME as startTime,
    END_TIME as endTime,
    TAG as tag

FROM OA_BADGES bdg
    INNER JOIN OA_DEFINITIONS odef ON bdg.BADGE_ID = odef.ID
WHERE
    bdg.USER_ID = :userId
    AND
    odef.IS_ACTIVE = 1