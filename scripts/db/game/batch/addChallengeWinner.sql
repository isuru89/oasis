INSERT INTO OA_CHALLENGE_WINNER (
    user_id,
    team_id,
    team_scope_id,
    challenge_id,
    points,
    win_no,
    won_at,
    source_id,
    game_id
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :challengeId,
    :points,
    :winNo,
    :wonAt,
    :sourceId,
    :gameId
)
ON DUPLICATE KEY
UPDATE points = VALUES(points),
    won_at = VALUES(won_at),
    team_id = VALUES(team_id),
    team_scope_id = VALUES(team_scope_id)