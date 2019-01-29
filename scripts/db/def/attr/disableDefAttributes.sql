UPDATE OA_DEFINITION_ATTR
SET
    is_active = 0
WHERE
    def_id = :defId