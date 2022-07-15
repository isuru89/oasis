SELECT
    id,
    display_name AS displayName,
    email,
    avatar_ref AS avatarRef,
    timezone,
    gender,
    version,
    created_at AS createdAt,
    updated_at AS updatedAt,
    is_active AS active
FROM
    OA_PLAYER
WHERE
    id = :id