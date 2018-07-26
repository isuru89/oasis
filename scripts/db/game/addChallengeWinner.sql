INSERT INTO OA_CHALLENGE_WINNER (
    user_id,
    team_id,
    challenge_id,
    points,
    won_at
) VALUES (
    :userId,
    :teamId,
    :challengeId,
    :points,
    :wonAt
)