INSERT INTO OA_MILESTONE (
    user_id,
    team_id,
    event_type,
    ext_id,
    ts,
    milestone_id,
    level,
    max_level,
    game_id
) VALUES (
    :userId,
    :teamId,
    :eventType,
    :extId,
    :ts,
    :milestoneId,
    :level,
    :maxLevel,
    :gameId
)
ON DUPLICATE KEY
UPDATE ext_id = VALUES(ext_id),
    max_level = VALUES(max_level)