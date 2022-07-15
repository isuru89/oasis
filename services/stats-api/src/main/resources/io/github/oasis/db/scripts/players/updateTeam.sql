UPDATE
    OA_TEAM
SET
    avatar_ref = :avatarRef,
    color_code = :colorCode,
    version = version + 1,
    updated_at = :ts
WHERE
    id = :id
    AND
    version = :version