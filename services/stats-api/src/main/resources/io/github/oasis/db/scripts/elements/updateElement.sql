UPDATE
    OA_ELEMENT
SET
    impl = :impl,
    type = :type,
    name = :name,
    description = :description,
    updated_at = :ts
WHERE
    def_id = :defId