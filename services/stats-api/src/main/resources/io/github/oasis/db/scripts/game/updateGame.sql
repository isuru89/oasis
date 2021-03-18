UPDATE
    OA_GAME
SET
    description = :description,
    motto = :motto,
    logo_ref = :logoRef,
    status = :newGameStatus
WHERE
    id = :id