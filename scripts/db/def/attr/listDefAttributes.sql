SELECT
    def_id AS defId,
    def_sub_id AS defSubId,
    attribute_id AS attrId

FROM OA_DEFINITION_ATTR
WHERE
    def_id = :defId
    AND
    is_active = 1