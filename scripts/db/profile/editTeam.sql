UPDATE OA_TEAM
SET
    name = :name,
    avatar_ref = :avatarId,
    team_scope = :teamScope
WHERE
    team_id = :teamId