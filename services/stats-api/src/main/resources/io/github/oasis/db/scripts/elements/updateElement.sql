UPDATE
    OA_ELEMENT
SET
    name = :name,
    description = :description,
    updated_at = :ts
WHERE
    def_id = :defId