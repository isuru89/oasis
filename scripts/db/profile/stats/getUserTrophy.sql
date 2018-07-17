SELECT
    USER_ID as userId,
    THROPY_ID as trophyId,
    RANKING_POS as rankingPos,
    TS as achievedTime

FROM OA_TROPHY
WHERE
    USER_ID = :userId
    AND
    IS_ACTIVE = 1