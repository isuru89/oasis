INSERT INTO OA_POINTS (
    USER_ID,
    EVENT_TYPE,
    EXT_ID,
    TS,
    POINT_ID,
    SUB_POINT_ID,
    POINTS,
    TAG
) VALUES (
    :userId,
    :eventType,
    :extId,
    :ts,
    :pointId,
    :subPointId,
    :points
    :tag
)