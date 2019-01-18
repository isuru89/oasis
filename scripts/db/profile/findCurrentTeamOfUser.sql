SELECT
    otu.id AS id,
    otu.user_id AS userId,
    otu.team_id AS teamId,
    otu.role_id as roleId,
    oats.scope_id AS scopeId,
    otu.since AS joinedTime,
    otu.until AS deallocatedTime,
    otu.is_approved AS approved,
    ot.is_auto_team AS autoTeam

FROM OA_TEAM_USER otu
    LEFT JOIN OA_TEAM ot ON otu.team_id = ot.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON ot.team_scope = oats.scope_id

WHERE
    otu.user_id = :userId
    <if(checkApproved)>
    AND
    otu.is_approved = 1
    <endif>
    AND
    :currentEpoch >= otu.since
    AND
    (
    otu.until IS NULL
    OR
    otu.until > :currentEpoch
    )
ORDER BY
    otu.since DESC
LIMIT
    1