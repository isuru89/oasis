UPDATE OA_EVENT_SOURCE
SET
    is_downloaded = 1
WHERE
    id = :id
    AND
    is_downloaded = 0
    AND
    is_active = 1
    AND
    is_internal = 0