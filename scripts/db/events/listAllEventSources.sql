SELECT
    id AS id,
    source_name AS sourceName,
    nonce AS nonce,
    token AS token,
    key_secret AS secretKey,
    key_public AS publicKey,
    display_name AS displayName,
    is_downloaded AS downloaded,
    is_internal AS `internal`,
    is_active AS active,
    created_at AS createdAt,
    updated_at AS updatedAt

FROM OA_EVENT_SOURCE
WHERE
    is_active = 1