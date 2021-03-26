UPDATE
    OA_GAME
SET
    description = :description,
    motto = :motto,
    logo_ref = :logoRef,
    updated_at = :ts
WHERE
    id = :id