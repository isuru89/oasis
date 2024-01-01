SELECT
    id,
    name,
    description,
    motto,
    logo_ref AS logoRef,
    version AS version,
    start_at AS startTime,
    end_at AS endTime,
    created_at AS createdAt,
    updated_at AS updatedAt,
    is_active As active
FROM
    OA_GAME
WHERE
    id = :id