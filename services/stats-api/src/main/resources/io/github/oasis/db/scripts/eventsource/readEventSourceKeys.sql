SELECT
    public_key AS publicKey,
    private_key AS privateKey
FROM
    OA_EVENT_SOURCE_KEY
WHERE
    event_source_id = :id