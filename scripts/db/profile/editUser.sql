UPDATE OA_USER
SET
    user_name = :name,
    avatar_id = :avatarId,
    is_male = :isMale
WHERE
    user_id = :userId