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
    source_id,
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
    :sourceId,
    :tag
)
ON CONFLICT (user_id, event_type, ts, badge_id, sub_badge_id)
DO UPDATE SET tag = excluded.tag