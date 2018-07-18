SELECT
    otu.user_id AS userId,
    otu.team_id AS teamId,
    otu.since AS joinedTime,

FROM OA_TEAM_USER otu
    INNER JOIN OA_TEAM ot ON otu.team_id = ot.id
WHERE
    otu.user_id = :userId
    AND
    otu.since < :currentEpoch
ORDER BY
    otu.since DESC
LIMIT
    1