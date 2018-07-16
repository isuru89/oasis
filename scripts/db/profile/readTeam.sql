SELECT
    TEAM_ID as teamId,
    TEAM_SCOPE as teamScope,
    NAME as name,
    AVATAR_ID as avatarId,
    CREATED_AT as createdAt,
    UPDATED_AT as updatedAt
FROM OA_TEAMS
WHERE TEAM_ID = :teamId
LIMIT 1