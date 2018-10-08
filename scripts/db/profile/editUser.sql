UPDATE OA_USER
SET
    user_name = :name,
    avatar_ref = :avatarId,
    is_male = :isMale
WHERE
    user_id = :userId