INSERT INTO OA_PURCHASE (
    item_id,
    team_id,
    team_scope_id,
    user_id,
    for_hero,
    via_friend

) VALUES (
    :itemId,
    :teamId,
    :teamScopeId,
    :userId,
    :forHero,
    1
)