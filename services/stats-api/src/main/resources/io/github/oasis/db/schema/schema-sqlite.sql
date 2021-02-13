DROP TABLE IF EXISTS `OA_PLAYER`;
CREATE TABLE `OA_PLAYER` (
  `id` INTEGER PRIMARY KEY autoincrement,
  `display_name` varchar(64) NOT NULL,
  `email` varchar(64) NOT NULL UNIQUE,
  `avatar_ref` varchar(255) DEFAULT NULL,
  `timezone` varchar(64) NOT NULL,
  `gender` int DEFAULT NULL,
  `created_at` bigint DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1'
);

DROP TABLE IF EXISTS `OA_TEAM`;
CREATE TABLE `OA_TEAM` (
  `id` INTEGER PRIMARY KEY autoincrement,
  `name` varchar(64) NOT NULL COLLATE nocase,
  `motto` varchar(255) DEFAULT NULL,
  `avatar_ref` varchar(255) DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  `created_at` bigint DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `game_id` int NOT NULL
);