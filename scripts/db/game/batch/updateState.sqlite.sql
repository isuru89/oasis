INSERT INTO OA_RATING (
    user_id,
    team_id,
    team_scope_id,
    rating_id,
    current_state,
    current_state_name,
    current_value,
    current_points,
    is_currency,
    ext_id,
    game_id,
    source_id,
    changed_at
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :ratingId,
    :currState,
    :currStateName,
    :currValue,
    :currPoints,
    :isCurrency,
    :extId,
    :gameId,
    :sourceId,
    :changedAt
)
ON CONFLICT (user_id, team_id, rating_id)
DO UPDATE SET current_state = excluded.current_state,
    current_state_name = excluded.current_state_name,
    current_value = excluded.current_value,
    current_points = excluded.current_points,
    changed_at = excluded.changed_at,
    ext_id = excluded.ext_id