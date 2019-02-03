INSERT INTO OA_CHALLENGE_WINNER (
    user_id,
    team_id,
    team_scope_id,
    challenge_id,
    points,
    won_at,
    source_id,
    game_id
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :challengeId,
    :points,
    :wonAt,
    :sourceId,
    :gameId
)
ON CONFLICT (user_id, challenge_id)
DO UPDATE SET points = excluded.points,
    won_at = excluded.won_at,
    team_id = excluded.team_id,
    team_scope_id = excluded.team_scope_id