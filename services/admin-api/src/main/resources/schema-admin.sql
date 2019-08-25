CREATE TABLE IF NOT EXISTS OA_EXT_APP (
    app_id          INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(127),
    token           CHAR(32),
    key_secret      BLOB,
    key_public      BLOB,
    is_internal     TINYINT(1) DEFAULT 0,
    is_downloaded   TINYINT(1) DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_EXT_APP_EVENT (
    app_id          INT,
    event_type      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS OA_EXT_APP_GAME (
    app_id          INT,
    game_id         INT
);

CREATE TABLE IF NOT EXISTS OA_GAME_DEF (
    game_id         INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(64),
    description     VARCHAR(1024)
);
