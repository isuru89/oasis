SELECT
    oda.def_id AS defId,
    oda.def_sub_id AS defSubId,
    oda.attribute_id AS attrId

FROM OA_DEFINITION_ATTR oda
    INNER JOIN OA_DEFINITION od ON od.id = oda.def_id
WHERE
    od.game_id = :gameId
    AND
    oda.is_active = 1
    AND
    od.is_active = 1