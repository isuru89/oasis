INSERT INTO OA_PLAYER
(display_name, email, timezone, avatar_ref, gender, version, created_at, updated_at)
VALUES
(:displayName, :email, :timeZone, :avatarRef, :gender, 1, :ts, :ts);