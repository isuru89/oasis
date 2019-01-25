INSERT INTO OA_STATE (
    user_id,
    team_id,
    team_scope_id,
    state_id,
    current_state,
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
    :currValue,
    :currPoints,
    :isCurrency,
    :extId,
    :gameId,
    :sourceId,
    :changedAt
)
ON DUPLICATE KEY
UPDATE current_state = VALUES(current_state),
    current_value = VALUES(current_value),
    current_points = VALUES(current_points),
    changed_at = VALUES(changed_at),
    ext_id = VALUES(ext_id)