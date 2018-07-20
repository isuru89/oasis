SELECT
    id AS id,
    kind AS kind,
    name AS name,
    display_name AS displayName,
    content_data AS content,
    game_id AS gameId,
    parent_id AS parentId,
    is_active AS isActive,
    expiration_at AS expiration,
    created_at AS createdAt,
    updated_at AS updatedAt

FROM OA_DEFINITION
WHERE
    kind = :type
    AND
    game_id = :gameId
    AND
    is_active = 1