SELECT
    pointSummary.userId,
    COALESCE(pointSummary.teamId, 0) as teamId,
    oat.name as teamName,
    oat.team_scope as teamScopeId,
    oats.display_name as teamScopeName,
    pointSummary.totalPoints,
    NULL as totalBadges,
    NULL as totalUniqueBadges,
    NULL as totalChallengeWins
FROM
(
    SELECT
        oap.user_id as userId,
        oap.team_id as teamId,
        ROUND(SUM(oap.points), 2) as totalPoints
    FROM OA_POINTS oap
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
    badgeSummary.totalBadges,
    badgeSummary.totalUniqueBadges,
    NULL as totalChallengeWins
FROM
(
    SELECT
        oab.user_id as userId,
        oab.team_id as teamId,
        COUNT(*) as totalBadges,
        COUNT(DISTINCT oab.badge_id) as totalUniqueBadges
    FROM OA_BADGES oab
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
    NULL as totalBadges,
    NULL as totalUniqueBadges,
    challengeSummary.totalChallengeWins
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