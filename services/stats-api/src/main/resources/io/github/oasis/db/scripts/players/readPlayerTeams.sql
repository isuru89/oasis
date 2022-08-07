 SELECT
    ot.id,
    ot.game_id AS gameId,
    ot.name,
    ot.color_code AS colorCode,
    ot.avatar_ref AS avatarRef,
    ot.created_at AS createdAt,
    ot.updated_at AS updatedAt,
    ot.is_active AS active
FROM
    OA_PLAYER_TEAM oat
    INNER JOIN OA_TEAM ot ON oat.team_id = ot.id
WHERE
    oat.player_id = :playerId