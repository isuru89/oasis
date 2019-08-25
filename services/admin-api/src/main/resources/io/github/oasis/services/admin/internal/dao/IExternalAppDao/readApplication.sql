SELECT
    app_id AS id,
    name,
    token,
    key_secret AS keySecret,
    key_public AS keyPublic,
    is_downloaded AS downloaded,
    is_internal AS internal
FROM OA_EXT_APP
WHERE
    app_id = :appId
    AND
    is_active = true
