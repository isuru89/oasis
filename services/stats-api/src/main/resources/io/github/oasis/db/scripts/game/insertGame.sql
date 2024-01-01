INSERT INTO OA_GAME (
  name,
  motto,
  description,
  logo_ref,
  version,
  start_at,
  end_at,
  created_at,
  updated_at
)
VALUES (
  :name,
  :motto,
  :description,
  :logoRef,
  1,
  :startTime,
  :endTime,
  :ts,
  :ts
)