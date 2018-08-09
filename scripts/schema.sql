CREATE TABLE IF NOT EXISTS OA_POINTS (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    event_type      VARCHAR(256),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    point_id        INT,
    point_name      VARCHAR(1024),
    points          FLOAT(4),
    tag             VARCHAR(512),
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_BADGES (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    event_type      VARCHAR(256),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    badge_id        INT,
    sub_badge_id    VARCHAR(64),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    start_ext_id    VARCHAR(1024),
    end_ext_id      VARCHAR(1024),
    start_time      BIGINT,
    end_time        BIGINT,
    tag             VARCHAR(1024),
    is_active       TINYINT(1) DEFAULT 1
);

ALTER TABLE OA_BADGES ADD PRIMARY KEY (user_id, event_type, ts, badge_id, sub_badge_id);


CREATE TABLE IF NOT EXISTS OA_MILESTONES (
    user_id         INT,
    team_id         INT,
    event_type      VARCHAR(256),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    milestone_id    INT,
    level           INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active       TINYINT(1) DEFAULT 1
);

ALTER TABLE OA_MILESTONES ADD PRIMARY KEY (user_id, milestone_id, level);


CREATE TABLE IF NOT EXISTS OA_MILESTONE_STATE (
    user_id         INT,
    milestone_id    INT,
    current_val     FLOAT(4),
    current_val_i   BIGINT,
    next_val        FLOAT(4),
    next_val_i      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE OA_MILESTONE_STATE ADD PRIMARY KEY (user_id, milestone_id);


CREATE TABLE IF NOT EXISTS OA_DEFINITION (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    kind            INT,
    name            VARCHAR(1024),
    display_name    VARCHAR(1024),
    content_data    TEXT,
    game_id         BIGINT,
    parent_id       BIGINT,
    is_active       TINYINT(1) DEFAULT 1,
    expiration_at   BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_USER (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
    user_name       VARCHAR(1024),
    ext_id          BIGINT,
    email           VARCHAR(512),
    avatar_id       VARCHAR(1024),
    is_male         TINYINT(1),
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM (
    team_id         INT PRIMARY KEY AUTO_INCREMENT,
    team_scope      INT,
    name            VARCHAR(128),
    avatar_id       VARCHAR(1024),
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM_SCOPE (
    scope_id        INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(128),
    display_name    VARCHAR(255),
    ext_id          BIGINT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM_USER (
    team_id         INT,
    user_id         INT,
    role_id         INT DEFAULT 8,
    since           BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_USER_SECRET (
    user_id         INT,
    token           VARCHAR(4096),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_CHALLENGE_WINNER (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    challenge_id    INT,
    points          FLOAT(4),
    won_at          BIGINT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_SHOP_ITEM (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(64),
    description     VARCHAR(2048),
    scope           VARCHAR(32),
    level           INT,
    price           FLOAT(4),
    image_ref       VARCHAR(512),
    is_active       TINYINT(1) DEFAULT 1,
    expiration_at   BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_PURCHASE (
    item_id         INT,
    user_id         INT,
    cost            FLOAT(4),
    shared_at       BIGINT,
    via_friend      TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_JOBS (
    def_id          INT,
    jar_id          VARCHAR(2048),
    job_id          VARCHAR(2048),
    snapshot_dir    VARCHAR(2048),
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
