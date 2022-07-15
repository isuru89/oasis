UPDATE
    OA_ELEMENT
SET
    name = :name,
    description = :description,
    version = version + 1,
    updated_at = :ts
WHERE
    def_id = :defId
    AND
    version = :version