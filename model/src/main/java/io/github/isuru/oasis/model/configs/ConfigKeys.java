package io.github.isuru.oasis.model.configs;

/**
 * @author iweerarathna
 */
public class ConfigKeys {

    public static final String KEY_LOCATION = "_location";

    public static final String KEY_SOURCE_TYPE = "source.type";
    public static final String KEY_SOURCE_FILE = "source.file";
    public static final String KEY_OUTPUT_TYPE = "output.type";

    public static final String KEY_FLINK_PARALLELISM = "flink.parallelism";

    public static final String KEY_GAME_RULE_FILE = "game.rule.file";

    public static final String KEY_CHECKPOINT_ENABLED = "checkpoint.enabled";
    public static final String KEY_CHECKPOINT_INTERVAL = "checkpoint.interval";
    public static final String KEY_CHECKPOINT_DIR = "checkpoint.dir";

    public static final String KEY_JDBC_SCRIPTS_PATH = "oasis.db.scripts.path";
    public static final String KEY_JDBC_URL = "oasis.db.url";
    public static final String KEY_JDBC_USERNAME = "oasis.db.username";
    public static final String KEY_JDBC_PASSWORD = "oasis.db.password";
    public static final String KEY_JDBC_AUTO_SCHEMA = "oasis.db.autoSchema";
    public static final String KEY_JDBC_SCHEMA_DIR = "oasis.db.schemaDir";

    public static final String KEY_STORAGE_DIR = "oasis.storage.dir";
    public static final String KEY_EXEC_PARALLELISM = "oasis.exec.parallelism";

    public static final String KEY_RABBIT_HOST = "rabbit.host";
    public static final String KEY_RABBIT_PORT = "rabbit.port";
    public static final String KEY_RABBIT_VIRTUAL_HOST = "rabbit.virtualhost";

    public static final String KEY_RABBIT_SRVW_USERNAME = "rabbit.username.servicew";
    public static final String KEY_RABBIT_SRVW_PASSWORD = "rabbit.password.servicew";
    public static final String KEY_RABBIT_SRVR_USERNAME = "rabbit.username.servicer";
    public static final String KEY_RABBIT_SRVR_PASSWORD = "rabbit.password.servicer";
    public static final String KEY_RABBIT_INJ_USERNAME = "rabbit.username.injector";
    public static final String KEY_RABBIT_INJ_PASSWORD = "rabbit.password.injector";
    public static final String KEY_RABBIT_GSRC_USERNAME = "rabbit.username.gsource";
    public static final String KEY_RABBIT_GSRC_PASSWORD = "rabbit.password.gsource";
    public static final String KEY_RABBIT_GSNK_USERNAME = "rabbit.username.gsink";
    public static final String KEY_RABBIT_GSNK_PASSWORD = "rabbit.password.gsink";

    public static final String KEY_RABBIT_SRC_EXCHANGE_NAME = "rabbit.src.exchange";
    public static final String KEY_RABBIT_SRC_EXCHANGE_TYPE = "rabbit.src.exchange.type";
    public static final String KEY_RABBIT_SRC_EXCHANGE_DURABLE = "rabbit.src.exchange.durable";

    public static final String KEY_RABBIT_QUEUE_OUTPUT_DURABLE = "rabbit.queue.output.durable";
    public static final String KEY_RABBIT_QUEUE_SRC = "rabbit.queue.src";

    public static final String KEY_RABBIT_QUEUE_OUT_POINTS = "rabbit.queue.points";
    public static final String KEY_RABBIT_QUEUE_OUT_BADGES = "rabbit.queue.badges";
    public static final String KEY_RABBIT_QUEUE_OUT_MILESTONES = "rabbit.queue.milestones";
    public static final String KEY_RABBIT_QUEUE_OUT_MILESTONESTATES = "rabbit.queue.milestonestates";
    public static final String KEY_RABBIT_QUEUE_OUT_CHALLENGES = "rabbit.queue.challenges";
    public static final String KEY_RABBIT_QUEUE_OUT_STATES = "rabbit.queue.states";

    public static final String KEY_LOCAL_REF_SOURCE = "oasis.local.source.ref";
    public static final String KEY_LOCAL_REF_OUTPUT = "oasis.local.output.ref";

    public static final int DEF_RABBIT_PORT = 5672;
    public static final String DEF_RABBIT_VIRTUAL_HOST = "oasis";
    public static final String DEF_RABBIT_SRC_EXCHANGE_TYPE = "fanout";
    public static final boolean DEF_RABBIT_SRC_EXCHANGE_DURABLE = true;

    public static final String DEF_RABBIT_Q_POINTS_SINK = "game.o{gid}.points";
    public static final String DEF_RABBIT_Q_BADGES_SINK = "game.o{gid}.badges";
    public static final String DEF_RABBIT_Q_MILESTONES_SINK = "game.o{gid}.milestones";
    public static final String DEF_RABBIT_Q_MILESTONESTATE_SINK = "game.o{gid}.milestonestates";
    public static final String DEF_RABBIT_Q_CHALLENGES_SINK = "game.o{gid}.challenges";
    public static final String DEF_RABBIT_Q_STATES_SINK = "game.o{gid}.states";
}
