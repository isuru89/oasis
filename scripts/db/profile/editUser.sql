UPDATE OA_USERS
SET
    NAME = :name,
    AVATAR_ID = :avatarId,
    IS_MALE = :isMale
WHERE
    USER_ID = :userId