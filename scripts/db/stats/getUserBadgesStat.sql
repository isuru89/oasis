SELECT
    user_id as userId,
    badge_id as badgeId,
    sub_badge_id as subBadgeId,
    COUNT(*) as badgeCount
FROM OA_BADGES
WHERE
    user_id = :userId AND is_active = 1
GROUP BY
    user_id, badge_id, sub_badge_id