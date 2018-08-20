SELECT
    oacw.user_id AS userId,
    oau.user_name AS userName,
    oau.email AS userEmail,
    oau.avatar_id AS userAvatar,
    oacw.team_id AS teamId,
    oat.name AS teamName,
    oacw.team_scope_id AS teamScopeId,
    oats.display_name AS teamScopeDisplayName,
    oacw.points AS pointsScored,
    oacw.won_at AS wonAt,
    oacw.game_id AS gameId

FROM OA_CHALLENGE_WINNER oacw
    LEFT JOIN OA_USER oau ON oacw.user_id = oau.user_id
    LEFT JOIN OA_TEAM oat ON oacw.team_id = oat.team_id
    LEFT JOIN OA_TEAM_SCOPE oats ON oacw.team_scope_id = oats.scope_id
WHERE
    oacw.challenge_id = :challengeId
    AND
    oacw.is_active = 1
