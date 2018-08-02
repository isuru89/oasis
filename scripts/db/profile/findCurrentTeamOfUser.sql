SELECT
    otu.user_id AS userId,
    otu.team_id AS teamId,
    oats.scope_id AS scopeId,
    otu.since AS joinedTime

FROM OA_TEAM_USER otu
    LEFT JOIN OA_TEAM ot ON otu.team_id = ot.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON ot.team_scope = oats.scope_id

WHERE
    otu.user_id = :userId
    AND
    otu.since < :currentEpoch
ORDER BY
    otu.since DESC
LIMIT
    1