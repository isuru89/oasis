SELECT
    ID as id,
    USER_ID as userId,
    EVENT_TYPE as eventType,
    EXT_ID as externalId,
    TS as achievedTime,

FROM OA_BADGES bdg
    INNER JOIN OA_DEFINITIONS odef ON bdg.BADGE_ID = odef.ID
WHERE
    bdg.USER_ID = :userId
    AND
    odef.IS_ACTIVE = 1