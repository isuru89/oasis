SELECT
    ext_id as id,
    name as name,
    avatar_id as avatarId,
    is_male as male,
    is_active as isActive
FROM OA_USER
WHERE ext_id = :userId
LIMIT 1