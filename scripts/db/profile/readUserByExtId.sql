SELECT
    user_id as id,
    ext_id as extId,
    user_name as name,
    email as email,
    avatar_id as avatarId,
    is_male as male,
    is_active as active
FROM OA_USER
WHERE ext_id = :extId
LIMIT 1