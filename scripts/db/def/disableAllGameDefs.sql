UPDATE OA_DEFINITION
SET
    name = CONCAT(name, '-', LEFT(UUID(), 8)),
    is_active = 0
WHERE
    game_id = :gameId