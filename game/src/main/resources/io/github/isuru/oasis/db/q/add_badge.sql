INSERT INTO OA_BADGES (
    USER_ID,
    EVENT_TYPE,
    EXT_ID,
    TS,
    BADGE_ID,
    SUB_BADGE_ID,
    START_EXT_ID,
    END_EXT_ID
) VALUES (
    :userId,
    :eventType,
    :externalId,
    :ts,
    :badgeId,
    :subBadgeId,
    :startExtId,
    :endExtId
)