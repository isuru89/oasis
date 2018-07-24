SELECT
    *
FROM
    (
    SELECT
        tbl.user_id,
        tbl.month_m,
        tbl.month_y,
        tbl.totalPoints,
        (DENSE_RANK() over (PARTITION BY tbl.month_m, tbl.month_y ORDER BY tbl.totalPoints DESC)) AS 'rank'
    FROM
        (
        SELECT
            FROM_UNIXTIME(ts / 1000, '%m') AS month_m,
            FROM_UNIXTIME(ts / 1000, '%x') AS month_y,
            user_id,
            ROUND(SUM(points), 2) AS totalPoints
        FROM
            `OA_POINTS`
        GROUP BY
            month_m,
            month_y,
            user_id
        ) tbl
    ) t

WHERE
   t.user_id = 55