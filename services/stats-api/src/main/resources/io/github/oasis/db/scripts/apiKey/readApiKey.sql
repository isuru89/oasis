SELECT
    token,
    secret_key AS secretKey,
    roles
FROM
    OA_API_KEY
WHERE
    token = :token
    AND
    is_active = true