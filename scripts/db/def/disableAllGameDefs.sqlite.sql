UPDATE OA_DEFINITION
SET
    name = name || '-' || hex(RANDOMBLOB(16)),
    is_active = 0
WHERE
    game_id = :gameId