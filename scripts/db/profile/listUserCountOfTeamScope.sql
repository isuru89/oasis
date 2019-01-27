WITH currentTeams AS (
    SELECT
        user_id,
        MAX(since) AS since
    FROM OA_TEAM_USER
    GROUP BY user_id
)

SELECT
    oatu.team_scope_id AS id,
    COUNT(*) AS totalUsers

FROM currentTeams oact
    INNER JOIN OA_TEAM_USER oatu ON oatu.user_id = oact.user_id AND oatu.since = oact.since
    INNER JOIN OA_TEAM oat ON oat.team_id = oatu.team_id
WHERE
    oat.is_active = 1
GROUP BY
    oatu.team_scope_id


