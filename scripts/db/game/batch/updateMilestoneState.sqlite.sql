INSERT INTO OA_MILESTONE_STATE (
    user_id,
    milestone_id,
    current_val,
    current_val_i,
    next_val,
    next_val_i,
    game_id
) VALUES (
    :userId,
    :milestoneId,
    :valueDouble,
    :valueLong,
    :nextVal,
    :nextValInt,
    :gameId
)
ON CONFLICT (user_id, milestone_id)
DO UPDATE SET current_val = excluded.current_val,
    current_val_i = excluded.current_val_i,
    next_val = excluded.next_val,
    next_val_i = excluded.next_val_i
