UPDATE OA_DEFINITION
SET is_active = 0
WHERE
    game_id = :gameId