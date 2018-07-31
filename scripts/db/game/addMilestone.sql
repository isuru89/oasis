INSERT INTO OA_MILESTONES (
    user_id,
    team_id,
    event_type,
    ext_id,
    ts,
    milestone_id,
    level
) VALUES (
    :userId,
    :teamId,
    :eventType,
    :extId,
    :ts,
    :milestoneId,
    :level
)
ON DUPLICATE KEY
UPDATE ext_id = :extId