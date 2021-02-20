SELECT
    id,
    name,
    description,
    motto,
    logo_ref AS logoRef,
    created_at AS createdAt,
    updated_at AS updatedAt,
    is_active As active
FROM
    OA_GAME
LIMIT :pageSize OFFSET :offset