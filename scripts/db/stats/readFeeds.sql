SELECT
    game_id AS gameId,
    user_id AS userId,
    team_id AS teamId,
    team_scope_id AS teamScopeId,
    def_kind_id AS defKindId,
    def_id AS defId,
    action_id AS actionId,
    message AS message,
    sub_message AS subMessage,
    event_type AS eventType,
    caused_event AS causedEvent,
    tag AS tag,
    ts AS ts
FROM
    OA_FEED
WHERE
    1 = 1
    <if(hasUser)>
        AND user_id = :userId
    <endif>
    <if(hasTeam)>
        AND team_id = :teamId
    <endif>
    <if(hasTeamScope)>
        AND team_scope_id = :teamScopeId
    <endif>

    <if(hasStartRange)>
        AND ts >= :rangeStart
    <endif>
    <if(hasEndRange)>
        AND :rangeEnd > ts
    <endif>

LIMIT :size
OFFSET :offset
