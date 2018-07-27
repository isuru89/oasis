INSERT INTO OA_BADGES (
    user_id,
    team_id,
    event_type,
    ext_id,
    ts,
    badge_id,
    sub_badge_id,
    start_ext_id,
    end_ext_id,
    start_time,
    end_time,
    tag
) VALUES (
    :userId,
    :teamId,
    :eventType,
    :extId,
    :ts,
    :badgeId,
    :subBadgeId,
    :startExtId, :endExtId,
    :startTime, :endTime,
    :tag
)