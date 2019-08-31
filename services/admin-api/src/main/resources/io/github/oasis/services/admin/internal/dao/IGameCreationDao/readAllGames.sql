SELECT
    game_id AS id,
    name,
    description,
    current_state AS currentState
FROM OA_GAME_DEF
WHERE is_active = true