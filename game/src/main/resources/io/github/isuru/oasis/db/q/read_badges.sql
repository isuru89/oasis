SELECT *
FROM OA_BADGES
WHERE
    USER_ID = :userId
    AND EVENT_TYPE = :eventType
    AND BADGE_ID = :badgeId
    AND SUB_BADGE_ID = :subBadgeId