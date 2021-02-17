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
  `name` varchar(64) NOT NULL COLLATE nocase UNIQUE,
  `color_code` varchar(10) DEFAULT NULL,
  `avatar_ref` varchar(255) DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  `created_at` bigint DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `game_id` int NOT NULL
);

DROP TABLE IF EXISTS `OA_PLAYER_TEAM`;
CREATE TABLE `OA_PLAYER_TEAM` (
  `game_id` int NOT NULL,
  `team_id` int NOT NULL,
  `player_id` int NOT NULL,
  `created_at` bigint DEFAULT NULL,
  UNIQUE(`game_id`, `player_id`)
);

DROP TABLE IF EXISTS `OA_ELEMENT`;
CREATE TABLE `OA_ELEMENT` (
  `id` INTEGER PRIMARY KEY autoincrement,
  `type` varchar(64) NOT NULL,
  `game_id` int NOT NULL,
  `impl` varchar(255) NOT NULL,
  `def_id` varchar(128) NOT NULL COLLATE nocase UNIQUE,
  `name` varchar(128) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `created_at` bigint DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1'
);

DROP TABLE IF EXISTS `OA_ELEMENT_DATA`;
CREATE TABLE `OA_ELEMENT_DATA` (
  `element_id` int NOT NULL,
  `def_data` BLOB NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1'
);

DROP TABLE IF EXISTS `OA_ATTRIBUTE_DEF`;
CREATE TABLE `OA_ATTRIBUTE_DEF` (
  `id` INTEGER PRIMARY KEY autoincrement,
  `name` varchar(32) COLLATE nocase NOT NULL,
  `priority` int NOT NULL,
  `game_id` int NOT NULL,
  `color_code` varchar(10) DEFAULT NULL,
  UNIQUE(`game_id`, `name`)
);