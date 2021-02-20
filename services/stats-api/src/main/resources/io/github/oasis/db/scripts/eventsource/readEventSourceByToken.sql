SELECT
    id,
    token,
    display_name AS name,
    is_active AS active
FROM
    OA_EVENT_SOURCE
WHERE
    token = :token
    AND
    is_active = 1