SELECT
    id,
    name,
    description,
    motto,
    logo_ref AS logoRef,
    version AS version,
    created_at AS createdAt,
    updated_at AS updatedAt,
    is_active As active
FROM
    OA_GAME
WHERE
    is_active = true
LIMIT :pageSize OFFSET :offset