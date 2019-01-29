CREATE TABLE IF NOT EXISTS OA_POINT (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    event_type      VARCHAR(128),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    point_id        INT,
    point_name      VARCHAR(128),
    points          FLOAT(4),
    tag             VARCHAR(512),
    is_currency     TINYINT(1) DEFAULT 1,
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_BADGE (
    user_id         INT NOT NULL,
    team_id         INT,
    team_scope_id   INT,
    event_type      VARCHAR(128) NOT NULL,
    ext_id          VARCHAR(1024),
    ts              BIGINT NOT NULL,
    badge_id        INT NOT NULL,
    sub_badge_id    VARCHAR(64) NOT NULL,
    start_ext_id    VARCHAR(1024),
    end_ext_id      VARCHAR(1024),
    start_time      BIGINT,
    end_time        BIGINT,
    tag             VARCHAR(512),
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE OA_BADGE ADD PRIMARY KEY (user_id, event_type, ts, badge_id, sub_badge_id);


CREATE TABLE IF NOT EXISTS OA_MILESTONE (
    user_id         INT NOT NULL,
    team_id         INT,
    event_type      VARCHAR(128),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    milestone_id    INT NOT NULL,
    level           INT NOT NULL,
    is_active       TINYINT(1) DEFAULT 1,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE OA_MILESTONE ADD PRIMARY KEY (user_id, milestone_id, level);


CREATE TABLE IF NOT EXISTS OA_MILESTONE_STATE (
    user_id         INT NOT NULL,
    milestone_id    INT NOT NULL,
    current_val     FLOAT(4),
    current_val_i   BIGINT,
    next_val        FLOAT(4),
    next_val_i      BIGINT,
    loss_val        FLOAT(4),
    loss_val_i      BIGINT,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE OA_MILESTONE_STATE ADD PRIMARY KEY (user_id, milestone_id);

CREATE TABLE IF NOT EXISTS OA_STATE (
    user_id         INT NOT NULL,
    team_id         INT NOT NULL,
    team_scope_id   INT,
    state_id        INT NOT NULL,
    current_state   INT,
    current_value   VARCHAR(1024),
    current_points  FLOAT(4),
    ext_id          VARCHAR(1024),
    is_currency     TINYINT(1) DEFAULT 1,
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    changed_at      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_STATE ADD PRIMARY KEY (user_id, team_id, state_id);

CREATE TABLE IF NOT EXISTS OA_RACE (
    user_id         INT NOT NULL,
    team_id         INT NOT NULL,
    team_scope_id   INT,
    race_id         INT NOT NULL,
    race_start_at   BIGINT NOT NULL,
    race_end_at     BIGINT NOT NULL,
    rank_pos        INT,
    points          FLOAT(4),
    total_count     INT,
    awarded_points  FLOAT(4),
    awarded_at      BIGINT,
    game_id         INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE OA_RACE ADD PRIMARY KEY (user_id, team_id, race_id, race_start_at, race_end_at);

CREATE TABLE IF NOT EXISTS OA_DEFINITION (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    kind            INT,
    name            VARCHAR(128),
    display_name    VARCHAR(255),
    content_data    TEXT,
    game_id         BIGINT,
    parent_id       BIGINT,
    is_active       TINYINT(1) DEFAULT 1,
    expiration_at   BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_DEFINITION ADD UNIQUE (game_id, kind, name);

CREATE TABLE IF NOT EXISTS OA_ATTRIBUTE (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(32),
    display_name    VARCHAR(64),
    priority        INT,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_DEFINITION_ATTR (
    def_id          INT NOT NULL,
    def_sub_id      VARCHAR(64) NOT NULL,
    attribute_id    INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_DEFINITION_ATTR ADD PRIMARY KEY (def_id, def_sub_id);

CREATE TABLE IF NOT EXISTS OA_USER (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
    user_name       VARCHAR(512),
    nickname        VARCHAR(32),
    ext_id          BIGINT,
    email           VARCHAR(256) NOT NULL,
    avatar_ref      VARCHAR(1024),
    is_male         TINYINT(1),
    is_active       TINYINT(1) DEFAULT 1,
    is_auto_user    TINYINT(1) DEFAULT 0,
    is_activated    TINYINT(1) DEFAULT 0,
    last_logout_at  BIGINT,
    hero_id         INT,
    hero_updated    INT DEFAULT 0,
    hero_last_updated_at BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_USER ADD UNIQUE (email);

CREATE TABLE IF NOT EXISTS OA_USER_HERO (
    hero_id         INT PRIMARY KEY,
    display_name    VARCHAR(64),
    description     VARCHAR(1024),
    is_active       TINYINT(1) DEFAULT 1
);

CREATE TABLE IF NOT EXISTS OA_TEAM (
    team_id         INT PRIMARY KEY AUTO_INCREMENT,
    team_scope      INT,
    name            VARCHAR(128),
    avatar_ref      VARCHAR(1024),
    is_auto_team    TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_TEAM ADD UNIQUE (name);

CREATE TABLE IF NOT EXISTS OA_TEAM_SCOPE (
    scope_id        INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(128),
    display_name    VARCHAR(255),
    ext_id          BIGINT,
    is_auto_scope   TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE OA_TEAM_SCOPE ADD UNIQUE (name);

CREATE TABLE IF NOT EXISTS OA_TEAM_USER (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    team_id         INT,
    user_id         INT,
    role_id         INT DEFAULT 8,
    since           BIGINT,
    until           BIGINT,
    will_end_at     BIGINT,
    is_approved     TINYINT(1) DEFAULT 1,
    approved_at     BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_TEAM_SCOPE_USER (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    team_scope_id   INT,
    user_id         INT,
    role_id         INT DEFAULT 2,
    since           BIGINT,
    until           BIGINT,
    is_approved     TINYINT(1) DEFAULT 1,
    approved_at     BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_CHALLENGE_WINNER (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    challenge_id    INT,
    points          FLOAT(4),
    won_at          BIGINT,
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_SHOP_ITEM (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(64),
    description     VARCHAR(2048),
    scope           VARCHAR(32),
    level           INT,
    for_hero        INT,
    price           FLOAT(4),
    image_ref       VARCHAR(512),
    max_available   INT DEFAULT -1,
    limited_amount  TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    expiration_at   BIGINT,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_PURCHASE (
    item_id         INT,
    team_id         INT,
    team_scope_id   INT,
    user_id         INT,
    for_hero        INT,
    cost            FLOAT(4),
    purchased_at    BIGINT,
    shared_at       BIGINT,
    via_friend      TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_JOB (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    def_id          INT,
    jar_id          VARCHAR(2048),
    job_id          VARCHAR(2048),
    snapshot_dir    VARCHAR(2048),
    is_active       TINYINT(1) DEFAULT 1,
    to_be_finished_at BIGINT,
    state_data      BLOB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OA_EVENT_SOURCE (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    source_name     VARCHAR(128),
    nonce           INT,
    token           VARCHAR(64),
    key_secret      BLOB,
    key_public      BLOB,
    is_downloaded   TINYINT(1) DEFAULT 1,
    display_name    VARCHAR(1024),
    is_internal     TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
