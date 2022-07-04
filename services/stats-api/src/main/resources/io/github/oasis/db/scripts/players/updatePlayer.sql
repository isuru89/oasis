UPDATE
    OA_PLAYER
SET
    display_name = :displayName,
    avatar_ref = :avatarRef,
    gender = :gender,
    updated_at = :ts,
    is_active = :active
WHERE
    id = :id