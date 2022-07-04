SELECT
    game_id
FROM
    OA_EVENT_SOURCE_GAME oesg
    INNER JOIN OA_EVENT_SOURCE oes ON oesg.event_source_id = oes.id
WHERE
    oesg.event_source_id = :eventSourceId
    AND
    oes.is_active = true