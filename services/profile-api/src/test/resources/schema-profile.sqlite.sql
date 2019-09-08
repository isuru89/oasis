CREATE TABLE IF NOT EXISTS OA_USER (
    user_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    email           VARCHAR(255) NOT NULL,
    first_name      VARCHAR(64),
    last_name       VARCHAR(64),
    nickname        VARCHAR(32),
    avatar_ref      VARCHAR(1023),
    user_status     SMALLINT,
    gender          SMALLINT,
    is_active       TINYINT DEFAULT 1,
    is_auto_user    TINYINT DEFAULT 0,
    created_at      BIGINT
);

CREATE TABLE IF NOT EXISTS OA_TEAM (
    team_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name            VARCHAR(63) NOT NULL,
    motto           VARCHAR(255),
    avatar_ref      VARCHAR(255),
    is_active       TINYINT DEFAULT 1,
    created_at      BIGINT,

    UNIQUE (name COLLATE NOCASE)
);


CREATE TABLE IF NOT EXISTS OA_USER_TEAM (
    user_id         INT,
    team_id         INT,
    joined_at       BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_email ON OA_USER(email);
CREATE UNIQUE INDEX IF NOT EXISTS idx_team_name ON OA_TEAM(name);