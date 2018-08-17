INSERT INTO OA_USER (
    ext_id,
    user_name,
    email,
    avatar_id,
    is_aggregated,
    is_male
) VALUES (
    :extId,
    :name,
    :email,
    :avatarId,
    :aggregated,
    :male
)