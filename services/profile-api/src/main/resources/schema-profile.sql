CREATE TABLE IF NOT EXISTS OA_USER (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
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
    team_id         INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(63) NOT NULL,
    motto           VARCHAR(255),
    avatar_ref      VARCHAR(255),
    is_active       TINYINT DEFAULT 1,
    created_at      BIGINT
) DEFAULT COLLATE utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS OA_USER_TEAM (
    user_id         INT,
    team_id         INT,
    joined_at       BIGINT
);

ALTER TABLE OA_USER ADD UNIQUE (email);
ALTER TABLE OA_TEAM ADD UNIQUE (name);