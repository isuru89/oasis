UPDATE
    OA_GAME
SET
    status = :newGameStatus,
    updated_at = :ts
WHERE
    id = :id