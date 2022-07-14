SELECT
  OGS.game_id AS gameId,
  OGS.status,
  OGS.updated_at AS updatedAt
FROM
  OA_GAME_STATUS OGS
WHERE
  game_id = :id
ORDER BY
  updated_at DESC
LIMIT 1