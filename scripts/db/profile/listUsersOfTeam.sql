WITH currentTeams AS (
    SELECT
        user_id,
        MAX(since) AS since
    FROM OA_TEAM_USER
    GROUP BY user_id
)

SELECT
    oau.user_id as id,
    oau.ext_id as extId,
    oau.user_name as name,
    oau.email as email,
    oau.avatar_ref as avatarId,
    oau.is_male as male,
    oau.is_active as active
FROM currentTeams oact
    INNER JOIN OA_TEAM_USER oatu ON oatu.since = oact.since AND oatu.user_id = oact.user_id
    INNER JOIN OA_USER oau ON oatu.user_id = oau.user_id
WHERE
    oatu.team_id = :teamId
    AND
    oau.is_active = 1
ORDER BY
    oau.user_id
LIMIT
    :limit
OFFSET
    :offset