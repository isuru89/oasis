INSERT INTO OA_RACE (
    user_id,
    team_id,
    team_scope_id,
    race_id,
    race_start_at,
    race_end_at,
    rank_pos,
    points,
    awarded_at,
    game_id
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :raceId,
    :raceStartAt,
    :raceEndAt,
    :rankPos,
    :points,
    :awardedAt,
    :gameId
)
ON DUPLICATE KEY
UPDATE awarded_at = :awardedAt,
       rank_pos = :rankPos,
       points = :points