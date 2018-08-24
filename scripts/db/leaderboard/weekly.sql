SELECT
    *
FROM
    (
    SELECT
        tbl.user_id,
        tbl.week_yw,
        tbl.totalPoints,
        (DENSE_RANK() over (PARTITION BY tbl.week_yw ORDER BY tbl.totalPoints DESC)) AS 'rank'
    FROM
        (
        SELECT
            FROM_UNIXTIME(ts / 1000, '%x-%v') AS week_yw,
            user_id,
            ROUND(SUM(points), 2) AS totalPoints
        FROM
            OA_POINT
        GROUP BY
            week_yw,
            user_id
        ) tbl
    ) t

WHERE
   t.user_id = 12
ORDER BY
    t.week_yw