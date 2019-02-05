SELECT
    pointSummary.userId,
    COALESCE(pointSummary.teamId, 0) as teamId,
    oat.name as teamName,
    oat.team_scope as teamScopeId,
    oats.display_name as teamScopeName,
    pointSummary.totalPoints AS totalPoints,
    pointSummary.totalCount AS totalCount,
    NULL as totalBadges,
    NULL as totalUniqueBadges,
    NULL as totalChallengeWins,
    NULL as totalRaceWins
FROM
(
    SELECT
        oap.user_id as userId,
        oap.team_id as teamId,
        ROUND(SUM(oap.points), 2) as totalPoints,
        COUNT(oap.points) AS totalCount
    FROM OA_POINT oap
    WHERE
        oap.user_id = :userId
        AND
        oap.is_active = 1
    GROUP BY
        oap.user_id,
        oap.team_id
) pointSummary
LEFT JOIN OA_TEAM oat ON oat.team_id = pointSummary.teamId
LEFT JOIN OA_TEAM_SCOPE oats ON oat.team_scope = oats.scope_id

UNION ALL

SELECT
    badgeSummary.userId,
    COALESCE(badgeSummary.teamId, 0) as teamId,
    oat.name as teamName,
    oat.team_scope as teamScopeId,
    oats.display_name as teamScopeName,
    NULL as totalPoints,
    NULL as totalCount,
    badgeSummary.totalBadges,
    badgeSummary.totalUniqueBadges,
    NULL as totalChallengeWins,
    NULL as totalRaceWins
FROM
(
    SELECT
        oab.user_id as userId,
        oab.team_id as teamId,
        COUNT(*) as totalBadges,
        COUNT(DISTINCT oab.badge_id) as totalUniqueBadges
    FROM OA_BADGE oab
    WHERE
        oab.user_id = :userId
        AND
        oab.is_active = 1
    GROUP BY
        oab.user_id,
        oab.team_id
) badgeSummary
LEFT JOIN OA_TEAM oat ON oat.team_id = badgeSummary.teamId
LEFT JOIN OA_TEAM_SCOPE oats ON oat.team_scope = oats.scope_id

UNION ALL

SELECT
    challengeSummary.userId,
    COALESCE(challengeSummary.teamId, 0) as teamId,
    oat.name as teamName,
    oat.team_scope as teamScopeId,
    oats.display_name as teamScopeName,
    NULL as totalPoints,
    NULL as totalCount,
    NULL as totalBadges,
    NULL as totalUniqueBadges,
    challengeSummary.totalChallengeWins,
    NULL as totalRaceWins
FROM
(
SELECT
    oac.user_id as userId,
    oac.team_id as teamId,
    COUNT(DISTINCT oac.challenge_id) as totalChallengeWins
FROM OA_CHALLENGE_WINNER oac
WHERE
    oac.user_id = :userId
    AND
    oac.is_active = 1
GROUP BY
    oac.user_id,
    oac.team_id
) challengeSummary
LEFT JOIN OA_TEAM oat ON oat.team_id = challengeSummary.teamId
LEFT JOIN OA_TEAM_SCOPE oats ON oat.team_scope = oats.scope_id

UNION ALL

SELECT
    raceSummary.userId,
    COALESCE(raceSummary.teamId, 0) as teamId,
    oat.name as teamName,
    oat.team_scope as teamScopeId,
    oats.display_name as teamScopeName,
    NULL AS totalPoints,
    NULL AS totalCount,
    NULL AS totalBadges,
    NULL AS totalUniqueBadges,
    NULL AS totalChallengeWins,
    raceSummary.totalRaceWins as totalRaceWins
FROM
(
SELECT
    oar.user_id AS userId,
    oar.team_id AS teamId,
    COUNT(DISTINCT oar.race_id) AS totalRaceWins
FROM OA_RACE oar
WHERE
    oar.user_id = :userId
    AND
    oar.is_active = 1
GROUP BY
    oar.user_id,
    oar.team_id
) raceSummary
LEFT JOIN OA_TEAM oat ON oat.team_id = raceSummary.teamId
LEFT JOIN OA_TEAM_SCOPE oats ON oat.team_scope = oats.scope_id