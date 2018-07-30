CREATE TABLE IF NOT EXISTS OA_POINTS (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    team_id         INT,
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
    team_id         INT,
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
    team_id         INT,
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

CREATE TABLE IF NOT EXISTS OA_TEAM_USER (
    team_id         INT,
    user_id         INT,
    since           BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_CHALLENGE_WINNER (
    user_id         INT,
    team_id         INT,
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
