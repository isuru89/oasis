SELECT
    ext_id AS id,
    name AS name,
    is_male AS isMale,
    created_at AS createdAt,
    updated_at AS updatedAt,

FROM OA_USER
WHERE
    email = :email