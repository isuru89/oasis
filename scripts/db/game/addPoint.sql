INSERT INTO OA_POINTS (
    user_id,
    team_id,
    team_scope_id,
    event_type,
    ext_id,
    ts,
    point_id,
    point_name,
    points,
    tag
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :eventType,
    :extId,
    :ts,
    :pointId,
    :pointName,
    :points,
    :tag
)