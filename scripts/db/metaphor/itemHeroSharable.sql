SELECT
    u1.userId AS user1Id,
    u1.heroId AS user1Hero,
    u2.userId AS user2Id,
    u2.heroId AS user2Hero
FROM
    (SELECT
        oau1.user_id AS userId,
        oau1.hero_id AS heroId
    FROM
        OA_USER oau1
    WHERE
        oau1.user_id = :fromUserId
    ) u1
    INNER JOIN
    (SELECT
        oau2.user_id AS userId,
        oau2.hero_id AS heroId
    FROM
        OA_USER oau2
    WHERE
        oau2.user_id = :toUserId
    ) u2 ON u1.heroId = u2.heroId