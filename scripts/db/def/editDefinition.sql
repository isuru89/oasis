UPDATE OA_DEFINITION
SET
    name = :name,
    display_name = :displayName,
    content_data = :content,
    game_id = :gameId,
    parent_id = :parentId

WHERE
    id = :id
    AND
    kind = :typeId