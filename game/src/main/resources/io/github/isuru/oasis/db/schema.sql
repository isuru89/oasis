CREATE TABLE IF NOT EXISTS OA_POINTS (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    event_type      VARCHAR(1024),
    ext_id          BIGINT,
    ts              BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    point_id        INT,
    sub_point_id    VARCHAR(1024),
    points          FLOAT(4),
    tag             VARCHAR(512),
    is_active       TINYINT(1) DEFAULT 1
);

CREATE TABLE IF NOT EXISTS OA_BADGES (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    event_type      VARCHAR(1024),
    ext_id          BIGINT(20),
    ts              BIGINT(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    badge_id        INT,
    sub_badge_id    VARCHAR(1024),
    start_ext_id    BIGINT,
    end_ext_id      BIGINT,
    start_time      BIGINT,
    end_time        BIGINT,
    tag             VARCHAR(1024),
    is_active       TINYINT(1) DEFAULT 1
);

CREATE TABLE IF NOT EXISTS OA_MILESTONES (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    event_type      VARCHAR(1024),
    ext_id          BIGINT,
    ts              BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    milestone_id    INT,
    level           INT,
    is_active       TINYINT(1) DEFAULT 1
);

CREATE TABLE IF NOT EXISTS OA_MILESTONE_STATE (
    user_id         INT,
    milestone_id    INT,
    current_val     FLOAT(4),
    current_val_i   BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_DEFINITION (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    kind            INT,
    name            VARCHAR(1024),
    display_name    VARCHAR(1024),
    content_data    TEXT,
    game_id         BIGINT,
    PARENT_ID       BIGINT,
    IS_ACTIVE       TINYINT(1) DEFAULT 1,
    EXPIRATION_AT   BIGINT,
    CREATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_USER (
    USER_ID         INT,
    NAME            VARCHAR(1024),
    EMAIL           VARCHAR(512),
    AVATAR_ID       VARCHAR(1024),
    IS_MALE         TINYINT(1),
    IS_ACTIVE       TINYINT(1) DEFAULT 1,
    CREATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM (
    TEAM_ID         INT,
    TEAM_SCOPE      INT,
    NAME            VARCHAR(128),
    AVATAR_ID       VARCHAR(1024),
    CREATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM_USER (
    TEAM_ID         INT,
    USER_ID         INT,
    SINCE           BIGINT,
    CREATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
