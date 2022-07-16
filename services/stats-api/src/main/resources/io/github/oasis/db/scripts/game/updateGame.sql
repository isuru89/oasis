UPDATE
    OA_GAME
SET
    description = :description,
    motto = :motto,
    logo_ref = :logoRef,
    version = version + 1,
    updated_at = :ts
WHERE
    id = :id
    AND
    version = :version