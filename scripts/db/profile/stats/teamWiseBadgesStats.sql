SELECT
    oab.user_id as userId,
    oab.team_id as teamId,
    COUNT(*) as totalBadges
FROM OA_BADGES oab
WHERE
    oab.user_id = :userId
    AND
    oab.is_active = 1
GROUP BY
    oab.user_id,
    oab.team_id

