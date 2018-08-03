SELECT
    *
FROM
    (
    SELECT
        tbl.user_id,
        tbl.team_id,
        tbl.month_ym,
        tbl.totalPoints,
        (DENSE_RANK() over (PARTITION BY tbl.month_ym ORDER BY tbl.totalPoints DESC)) AS 'rank'
    FROM
        (
        SELECT
            FROM_UNIXTIME(ts / 1000, '%x-%m') AS month_ym,
            user_id,
            team_id,
            ROUND(SUM(points), 2) AS totalPoints
        FROM
            `OA_POINTS`
        GROUP BY
            month_ym,
            user_id,
            team_id
        ) tbl
    ) t

WHERE
   t.user_id = 55
ORDER BY
    t.month_ym