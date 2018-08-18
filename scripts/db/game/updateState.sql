INSERT INTO OA_STATES (
    user_id,
    state_id,
    current_state,
    current_value,
    ext_id,
    game_id,
    changed_at
) VALUES (
    :userId,
    :stateId,
    :currState,
    :currValue,
    :extId,
    :gameId,
    :changedAt
)
ON DUPLICATE KEY
UPDATE current_state = :currState,
    current_value = :currValue,
    changed_at = :ts,
    ext_id = :extId