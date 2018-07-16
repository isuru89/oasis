SELECT
    NAME as name,
    AVATAR_ID as avatarId,
    IS_MALE as isMale,
    IS_ACTIVE as isActive
FROM OA_USERS
WHERE USER_ID = :userId
LIMIT 1