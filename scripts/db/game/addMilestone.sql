INSERT INTO OA_MILESTONE (
    user_id,
    team_id,
    event_type,
    ext_id,
    ts,
    milestone_id,
    level,
    game_id
) VALUES (
    :userId,
    :teamId,
    :eventType,
    :extId,
    :ts,
    :milestoneId,
    :level,
    :gameId
)
ON DUPLICATE KEY
UPDATE ext_id = :extId