DELETE FROM
    OA_PLAYER_TEAM
WHERE
    game_id = :gameId
    AND
    player_id = :playerId
    AND
    team_id = :teamId