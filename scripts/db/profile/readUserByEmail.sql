SELECT
    user_id as id,
    ext_id as extId,
    user_name as name,
    nickname as nickName,
    email as email,
    avatar_ref as avatarId,
    is_male as male,
    is_active as active,
    is_auto_user as autoUser,
    is_activated AS activated,
    last_logout_at as lastLogoutAt
FROM OA_USER
WHERE email = :email
LIMIT 1