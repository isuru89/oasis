INSERT INTO OA_STATES (
    user_id,
    team_id,
    team_scope_id,
    state_id,
    current_state,
    current_value,
    current_points,
    ext_id,
    game_id,
    changed_at
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :stateId,
    :currState,
    :currValue,
    :currPoints,
    :extId,
    :gameId,
    :changedAt
)
ON DUPLICATE KEY
UPDATE current_state = :currState,
    current_value = :currValue,
    current_points = :currPoints
    changed_at = :ts,
    ext_id = :extId