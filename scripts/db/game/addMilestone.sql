INSERT INTO OA_MILESTONES (
    user_id,
    event_type,
    ext_id,
    ts,
    milestone_id,
    level
) VALUES (
    :userId,
    :eventType,
    :extId,
    :ts,
    :milestoneId,
    :level
)