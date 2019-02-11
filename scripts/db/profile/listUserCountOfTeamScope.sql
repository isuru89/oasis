WITH currentTeams AS (
    SELECT
        user_id,
        MAX(since) AS since
    FROM OA_TEAM_USER
    WHERE
        is_approved = 1
        AND
        :atTime >= since
        AND
        (until IS NULL OR until > :atTime)
    GROUP BY user_id
)

SELECT
    oat.team_scope AS id,
    COUNT(*) AS totalUsers

FROM currentTeams oact
    INNER JOIN OA_TEAM_USER oatu ON oatu.user_id = oact.user_id AND oatu.since = oact.since
    INNER JOIN OA_TEAM oat ON oat.team_id = oatu.team_id
    INNER JOIN OA_USER oau ON oact.user_id = oau.user_id
WHERE
    oat.is_active = 1
    <if(hasAutoUsers)>
        AND oau.is_auto_user = :autoUsers
    <endif>
GROUP BY
    oat.team_scope


