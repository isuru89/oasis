INSERT INTO OA_STATE (
    user_id,
    team_id,
    team_scope_id,
    state_id,
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
    :stateId,
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
ON CONFLICT (user_id, team_id, state_id)
DO UPDATE SET current_state = excluded.current_state,
    current_state_name = excluded.current_state_name,
    current_value = excluded.current_value,
    current_points = excluded.current_points,
    changed_at = excluded.changed_at,
    ext_id = excluded.ext_id