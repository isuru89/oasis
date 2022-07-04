SELECT
    id,
    token,
    display_name AS name,
    is_active AS active
FROM
    OA_EVENT_SOURCE_GAME oesg
    INNER JOIN OA_EVENT_SOURCE oes ON oesg.event_source_id = oes.id
WHERE
    oesg.game_id = :gameId
    AND
    oes.is_active = true