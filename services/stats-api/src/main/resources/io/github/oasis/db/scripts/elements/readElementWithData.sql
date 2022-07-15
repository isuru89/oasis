SELECT
    oa.id,
    oa.type,
    oa.game_id AS gameId,
    oa.name AS elementName,
    oa.def_id AS elementId,
    oa.description AS elementDescription,
    oa.is_active AS active,
    oa.version AS version,
    oad.def_data AS data
FROM
    OA_ELEMENT oa
    LEFT JOIN OA_ELEMENT_DATA oad ON oa.id = oad.element_id
WHERE
    oa.def_id = :defId