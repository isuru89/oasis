UPDATE OA_DEFINITION
SET
    name = name || '-' || hex(RANDOMBLOB(16)),
    is_active = 0
WHERE
    id = :id
    OR
    parent_id = :id