UPDATE OA_EVENT_SOURCE
SET is_active = 0
WHERE
    id = :id
    AND
    is_internal = 0