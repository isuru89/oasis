DELETE FROM
    OA_PLAYER_TEAM
WHERE
    player_id = :playerId
    AND
    team_id = :teamId