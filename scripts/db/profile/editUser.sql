UPDATE OA_USER
SET
    name = :name,
    avatar_id = :avatarId,
    is_male = :isMale
WHERE
    ext_id = :userId