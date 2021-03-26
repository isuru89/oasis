SELECT
    id,
    type,
    impl,
    game_id AS gameId,
    name AS elementName,
    def_id AS elementId,
    description AS elementDescription,
    is_active AS active
FROM
    OA_ELEMENT
WHERE
    game_id = :gameId
    AND
    is_active = 1