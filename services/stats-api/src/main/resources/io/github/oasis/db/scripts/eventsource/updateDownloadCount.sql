UPDATE
    OA_EVENT_SOURCE_KEY
SET
    download_count = download_count + :downloadCount
WHERE
    event_source_id = :id