UPDATE
    OA_PLAYER
SET
    display_name = :displayName,
    avatar_ref = :avatarRef,
    gender = :gender,
    version = version + 1,
    updated_at = :ts,
    is_active = :active
WHERE
    id = :id
    AND
    version = :version