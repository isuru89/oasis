INSERT INTO OA_USER (
    ext_id,
    user_name,
    nickname,
    email,
    avatar_ref,
    is_auto_user,
    <if(isActivated)>
    is_activated,
    <endif>
    is_male
) VALUES (
    :extId,
    :name,
    :nickname,
    :email,
    :avatarId,
    :isAutoUser,
    <if(isActivated)>
    :activated,
    <endif>
    :male
)