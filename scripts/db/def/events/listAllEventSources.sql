SELECT
    id AS id,
    token AS token,
    display_name AS displayName,
    is_internal AS `internal`,
    is_active AS active,
    created_at AS createdAt,
    updated_at AS updatedAt

FROM OA_EVENT_SOURCE
WHERE
    is_active = 1