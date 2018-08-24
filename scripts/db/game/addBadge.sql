INSERT INTO OA_BADGE (
    user_id,
    team_id,
    team_scope_id,
    event_type,
    ext_id,
    ts,
    badge_id,
    sub_badge_id,
    start_ext_id,
    end_ext_id,
    start_time,
    end_time,
    game_id,
    tag
) VALUES (
    :userId,
    :teamId,
    :teamScopeId,
    :eventType,
    :extId,
    :ts,
    :badgeId,
    :subBadgeId,
    :startExtId, :endExtId,
    :startTime, :endTime,
    :gameId,
    :tag
)
ON DUPLICATE KEY
UPDATE tag = :tag