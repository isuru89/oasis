INSERT INTO OA_USER (
    ext_id,
    user_name,
    email,
    avatar_ref,
    is_auto_user,
    is_male
) VALUES (
    :extId,
    :name,
    :email,
    :avatarId,
    :isAutoUser,
    :male
)