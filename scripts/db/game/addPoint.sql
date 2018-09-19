INSERT INTO OA_POINT (
    user_id,
    team_id,
    team_scope_id,
    event_type,
    ext_id,
    ts,
    point_id,
    point_name,
    points,
    game_id,
    source_id,
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
    :gameId,
    :sourceId,
    :tag
)