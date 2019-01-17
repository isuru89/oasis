UPDATE OA_TEAM
SET
    name = :name,
    avatar_ref = :avatarId
WHERE
    team_id = :teamId