SELECT
    id,
    name,
    game_id AS gameId,
    color_code AS colorCode,
    priority
FROM
    OA_RANK_DEF
WHERE
    game_id = :gameId