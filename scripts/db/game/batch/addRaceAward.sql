INSERT INTO OA_RACE (
    user_id,
    team_id,
    team_scope_id,
    race_id,
    race_start_at,
    race_end_at,
    rank_pos,
    points,
    total_count,
    awarded_points,
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
    :totalPoints,
    :totalCount,
    :awardedPoints,
    :awardedAt,
    :gameId
)
ON DUPLICATE KEY
UPDATE awarded_at = VALUES(awarded_at),
       rank_pos = VALUES(rank_pos),
       points = VALUES(points)