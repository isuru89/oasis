CREATE TABLE  OA_POINT (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
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
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE  OA_BADGE (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    event_type      VARCHAR(128),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    badge_id        INT,
    sub_badge_id    VARCHAR(64),
    start_ext_id    VARCHAR(1024),
    end_ext_id      VARCHAR(1024),
    start_time      BIGINT,
    end_time        BIGINT,
    tag             VARCHAR(512),
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, event_type, ts, badge_id, sub_badge_id)
);


CREATE TABLE  OA_MILESTONE (
    user_id         INT,
    team_id         INT,
    event_type      VARCHAR(128),
    ext_id          VARCHAR(1024),
    ts              BIGINT,
    milestone_id    INT,
    level           INT,
    max_level       INT,
    is_active       TINYINT(1) DEFAULT 1,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, milestone_id, level)
);


CREATE TABLE  OA_MILESTONE_STATE (
    user_id         INT,
    milestone_id    INT,
    curr_base_val   FLOAT(4),
    curr_base_val_i BIGINT,
    current_val     FLOAT(4),
    current_val_i   BIGINT,
    next_val        FLOAT(4),
    next_val_i      BIGINT,
    loss_val        FLOAT(4),
    loss_val_i      BIGINT,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, milestone_id)
);

CREATE TABLE  OA_STATE (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    state_id        INT,
    current_state   INT,
    current_state_name VARCHAR(128),
    current_value   VARCHAR(1024),
    current_points  FLOAT(4),
    is_currency     TINYINT(1) DEFAULT 1,
    ext_id          VARCHAR(1024),
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    changed_at      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, team_id, state_id)
);

CREATE TABLE  OA_CHALLENGE_WINNER (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    challenge_id    INT,
    points          FLOAT(4),
    win_no          INT,
    won_at          BIGINT,
    game_id         INT,
    source_id       INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, challenge_id)
);

CREATE TABLE  OA_RACE (
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    race_id         INT,
    race_start_at   BIGINT,
    race_end_at     BIGINT,
    rank_pos        INT,
    points          FLOAT(4),
    total_count     INT,
    awarded_points  FLOAT(4),
    awarded_at      BIGINT,
    game_id         INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, team_id, race_id, race_start_at, race_end_at)
);

CREATE TABLE  OA_DEFINITION (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    kind            INT,
    name            VARCHAR(128),
    display_name    VARCHAR(255),
    content_data    TEXT,
    game_id         BIGINT,
    parent_id       BIGINT,
    is_active       TINYINT(1) DEFAULT 1,
    expiration_at   BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS OA_DEF_UNQ ON OA_DEFINITION (game_id, kind, name)
;

CREATE TABLE  OA_ATTRIBUTE (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            VARCHAR(32),
    display_name    VARCHAR(64),
    priority        INT,
    game_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE  OA_DEFINITION_ATTR (
    def_id          INT,
    def_sub_id      VARCHAR(64),
    attribute_id    INT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (def_id, def_sub_id)
);

CREATE TABLE  OA_USER (
    user_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name       VARCHAR(512),
    nickname        VARCHAR(32),
    ext_id          BIGINT,
    email           VARCHAR(256),
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
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS OA_USER_UNQ ON OA_USER (email)
;

CREATE TABLE  OA_USER_HERO (
    hero_id         INTEGER PRIMARY KEY,
    display_name    VARCHAR(64),
    description     VARCHAR(1024),
    is_active       TINYINT(1) DEFAULT 1
);

CREATE TABLE  OA_TEAM (
    team_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    team_scope      INT,
    name            VARCHAR(128),
    avatar_ref      VARCHAR(1024),
    is_auto_team    TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS OA_TEAM_UNQ ON OA_TEAM (name)
;

CREATE TABLE  OA_TEAM_SCOPE (
    scope_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name            VARCHAR(128),
    display_name    VARCHAR(255),
    ext_id          BIGINT,
    is_auto_scope   TINYINT(1) DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS OA_TEAM_SCOPE_UNQ ON OA_TEAM_SCOPE (name)
;

CREATE TABLE  OA_TEAM_USER (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
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

CREATE TABLE  OA_TEAM_SCOPE_USER (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    team_scope_id   INT,
    user_id         INT,
    role_id         INT DEFAULT 2,
    since           BIGINT,
    until           BIGINT,
    is_approved     TINYINT(1) DEFAULT 1,
    approved_at     BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE  OA_SHOP_ITEM (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
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

CREATE TABLE  OA_PURCHASE (
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

CREATE TABLE  OA_JOB (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    def_id          INT,
    jar_id          VARCHAR(2048),
    job_id          VARCHAR(2048),
    snapshot_dir    VARCHAR(2048),
    is_active       TINYINT(1) DEFAULT 1,
    to_be_finished_at BIGINT,
    state_data      BLOB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE  OA_EVENT_SOURCE (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
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
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE  OA_FEED (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id         INT,
    user_id         INT,
    team_id         INT,
    team_scope_id   INT,
    def_kind_id     INT,
    def_id          INT,
    action_id       INT,
    message         VARCHAR(512),
    sub_message     VARCHAR(1024),
    event_type      VARCHAR(128),
    caused_event    VARCHAR(1024),
    tag             VARCHAR(2048),
    ts              BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER TRIGGER_OA_DEF_UPD
    AFTER UPDATE ON OA_DEFINITION FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_DEFINITION SET updated_at=CURRENT_TIMESTAMP where id=OLD.id;
END
;
CREATE TRIGGER TRIGGER_OA_TEAM_SCOPE_UPD
    AFTER UPDATE ON OA_TEAM_SCOPE FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_TEAM_SCOPE SET updated_at=CURRENT_TIMESTAMP where scope_id=OLD.scope_id;
END
;
CREATE TRIGGER TRIGGER_OA_TEAM_UPD
    AFTER UPDATE ON OA_TEAM FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_TEAM SET updated_at=CURRENT_TIMESTAMP where team_id=OLD.team_id;
END
;
CREATE TRIGGER TRIGGER_OA_USER_UPD
    AFTER UPDATE ON OA_USER FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_USER SET updated_at=CURRENT_TIMESTAMP where user_id=OLD.user_id;
END
;
CREATE TRIGGER TRIGGER_OA_EVENT_SOURCE_UPD
    AFTER UPDATE ON OA_EVENT_SOURCE FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_EVENT_SOURCE SET updated_at=CURRENT_TIMESTAMP where id=OLD.id;
END
;
CREATE TRIGGER TRIGGER_OA_JOB_UPD
    AFTER UPDATE ON OA_JOB FOR EACH ROW WHEN NEW.updated_at \<= OLD.updated_at
 BEGIN
    UPDATE OA_JOB SET updated_at=CURRENT_TIMESTAMP where id=OLD.id;
END
;

