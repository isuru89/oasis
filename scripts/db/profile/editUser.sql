UPDATE OA_USER
SET
    user_name = :name,
    avatar_ref = :avatarId,
    is_male = :isMale,
    nickname = :nickname
WHERE
    user_id = :userId