INSERT INTO OA_EVENT_SOURCE (
    source_name,
    nonce,
    token,
    key_secret,
    key_public,
    is_downloaded,
    display_name,
    is_internal
) VALUES (
    :sourceName,
    :nonce,
    :token,
    :keySecret,
    :keyPublic,
    0,
    :displayName,
    :isInternal
)