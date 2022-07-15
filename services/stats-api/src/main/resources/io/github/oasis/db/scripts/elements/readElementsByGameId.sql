SELECT
    id,
    type,
    game_id AS gameId,
    name AS elementName,
    def_id AS elementId,
    description AS elementDescription,
    version AS version,
    is_active AS active
FROM
    OA_ELEMENT
WHERE
    game_id = :gameId
    AND
    is_active = true