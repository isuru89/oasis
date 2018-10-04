UPDATE OA_USER
SET
    hero_id = :heroId,
    hero_updated = hero_updated + 1,
    hero_last_updated_at = :heroUpdatedAt
WHERE
    user_id = :userId
    AND
    hero_updated >= 0
    AND
    hero_updated < :updateLimit
    AND
    is_active = 1
    AND
    hero_id <> :heroId