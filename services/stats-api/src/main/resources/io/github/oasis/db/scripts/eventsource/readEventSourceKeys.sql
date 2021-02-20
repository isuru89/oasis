SELECT
    public_key AS publicKey,
    private_key AS privateKey,
    download_count AS downloadCount
FROM
    OA_EVENT_SOURCE_KEY
WHERE
    event_source_id = :id