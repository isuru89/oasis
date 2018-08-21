INSERT INTO OA_MILESTONE_STATE (
    user_id,
    milestone_id,
    loss_val,
    loss_val_i
) VALUES (
    :userId,
    :milestoneId,
    :lossVal,
    :lossValInt
)
ON DUPLICATE KEY
UPDATE loss_val = :lossVal,
        loss_val_i = :lossValInt
