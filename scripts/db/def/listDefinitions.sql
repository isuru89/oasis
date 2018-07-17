SELECT
    ID as id,
    KIND as defKind,
    NAME as name,
    DISPLAY_NAME as displayName,
    CONTENT_DATA as content,
    GAME_ID as gameId,
    PARENT_ID as parentId,
    IS_ACTIVE as isActive,
    EXPIRATION_AT as expiration,
    CREATED_AT as createdAt,
    UPDATED_AT as updatedAt

FROM OA_DEFINITIONS
WHERE
    KIND = :type
    AND
    IS_ACTIVE = 1