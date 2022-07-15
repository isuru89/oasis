SELECT
    id,
    type,
    game_id AS gameId,
    name AS elementName,
    def_id AS elementId,
    version AS version,
    description AS elementDescription,
    is_active AS active
FROM
    OA_ELEMENT
WHERE
    def_id = :defId