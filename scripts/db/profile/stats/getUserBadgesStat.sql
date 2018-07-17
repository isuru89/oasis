SELECT
    USER_ID as userId,
    'Badges' as typeId,
    COUNT(*) as totalBadges
FROM OA_BADGES
WHERE
    USER_ID = :userId AND IS_ACTIVE = 1
GROUP BY USER_ID

UNION ALL

SELECT
    USER_ID as userId,
    'Last Week Badges' as typeId,
    COUNT(*) as totalPoints
FROM OA_BADGES
WHERE
    USER_ID = :userId
    AND
    IS_ACTIVE = 1
    AND
    TS >= :startDate
GROUP BY USER_ID