SELECT
    *,
    is_active AS active
FROM
    OA_PLAYER
WHERE
    email = :email