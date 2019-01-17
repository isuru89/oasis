INSERT INTO OA_TEAM_USER (
    team_id,
    user_id,
    role_id,
    <if(hasApproved)>
    is_approved,
    approved_at,
    <endif>
    since
) VALUES (
    :teamId,
    :userId,
    :roleId,
    <if(hasApproved)>
    :isApproved,
    :approvedAt,
    <endif>
    :since
)