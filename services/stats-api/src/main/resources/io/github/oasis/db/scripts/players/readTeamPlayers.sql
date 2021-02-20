SELECT
    op.id,
    op.display_name AS displayName,
    op.email,
    op.avatar_ref AS avatarRef,
    op.timezone,
    op.gender,
    op.created_at AS createdAt,
    op.updated_at AS updatedAt,
    op.is_active AS active
FROM
    OA_PLAYER_TEAM oat
    INNER JOIN OA_PLAYER op ON oat.player_id = op.id
WHERE
    oat.team_id = :teamId