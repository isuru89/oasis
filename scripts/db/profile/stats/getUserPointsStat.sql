SELECT
    USER_ID as userId,
    'Points' as typeId,
    SUM(POINTS) as totalPoints
FROM OA_POINTS
WHERE
    USER_ID = :userId AND IS_ACTIVE = 1
GROUP BY USER_ID

UNION ALL

SELECT
    USER_ID as userId,
    'Last Week Points' as typeId,
    SUM(POINTS) as totalPoints
FROM OA_POINTS
WHERE
    USER_ID = :userId
    AND
    IS_ACTIVE = 1
    AND
    TS >= :startDate
GROUP BY USER_ID