INSERT INTO OA_PURCHASE (
    item_id,
    team_id,
    team_scope_id,
    user_id,
    cost,
    purchased_at
) VALUES (
    :itemId,
    :teamId,
    :teamScopeId,
    :userId,
    :cost,
    :purchasedAt
)