SELECT
    id,
    game_id AS gameId,
    name,
    color_code AS colorCode,
    avatar_ref AS avatarRef,
    created_at AS createdAt,
    updated_at AS updatedAt,
    is_active AS active
FROM
    OA_TEAM
WHERE
    name LIKE :name || '%'
LIMIT :limit OFFSET :offset