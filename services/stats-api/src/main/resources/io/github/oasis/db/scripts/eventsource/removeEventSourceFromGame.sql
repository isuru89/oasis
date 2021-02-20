DELETE FROM
    OA_EVENT_SOURCE_GAME
WHERE
    game_id = :gameId
    AND
    event_source_id = :eventSourceId