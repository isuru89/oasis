INSERT INTO OA_CHALLENGE_WINNER (
    user_id,
    team_id,
    team_scope_id,
    challenge_id,
    points,
    won_at,
    game_id
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :challengeId,
    :points,
    :wonAt,
    :gameId
)