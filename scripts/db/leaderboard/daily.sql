SELECT
    *
FROM
    (
    SELECT
        tbl.user_id,
        tbl.day_d,
        tbl.totalPoints,
        (DENSE_RANK() over (PARTITION BY tbl.day_d ORDER BY tbl.totalPoints DESC)) AS 'rank'
    FROM
        (
        SELECT
            DATE(FROM_UNIXTIME(ts / 1000)) AS day_d,
            user_id,
            ROUND(SUM(points), 2) AS totalPoints
        FROM
            OA_POINT
        GROUP BY
            day_d,
            user_id
        ) tbl
    ) t

WHERE
   t.user_id = 12
ORDER BY
    t.day_d