UPDATE
    OA_GAME
SET
    description = :description,
    motto = :motto,
    logo_ref = :logoRef,
    start_at = :startTime,
    end_at = :endTime,
    version = version + 1,
    updated_at = :ts
WHERE
    id = :id
    AND
    version = :version