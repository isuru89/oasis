INSERT INTO OA_POINTS (
    user_id,
    event_type,
    ext_id,
    ts,
    point_id,
    sub_point_id,
    points,
    tag
) VALUES (
    :userId,
    :eventType,
    :extId,
    :ts,
    :pointId,
    :subPointId,
    :points,
    :tag
)