UPDATE OA_DEFINITION
SET is_active = 0
WHERE
    id = :id
    OR
    parent_id = :id