INSERT INTO OA_MILESTONES (
    USER_ID,
    EVENT_TYPE,
    EXT_ID,
    TS,
    MILESTONE_ID,
    LEVEL
) VALUES (
    :userId,
    :eventType,
    :externalId,
    :ts,
    :milestoneId,
    :level
)