package io.github.isuru.oasis.game.utils;

/**
 * @author iweerarathna
 */
public final class Constants {

    public static final String ENV_OASIS_GAME_ID = "OASIS_GAME_ID";

    public static final String KEY_LOCATION = "_location";

    public static final String KEY_KAFKA_HOST = "kafka.host";
    public static final String KEY_OUTPUT_TYPE = "output.type";

    public static final String KEY_PREFIX_SOURCE_KAFKA = "source.kafka.consumer.";
    public static final String KEY_PREFIX_OUTPUT_KAFKA = "output.kafka.producer.";
    public static final String KEY_KAFKA_SOURCE_TOPIC = "kafka.topic.consumer.name";

    public static final String KEY_SOURCE_TYPE = "source.type";
    public static final String KEY_SOURCE_FILE = "source.file";

    public static final String KEY_JDBC_INSTANCE = "jdbc.instance";

    public static final String KEY_CHECKPOINT_ENABLED = "checkpoint.enabled";
    public static final String KEY_CHECKPOINT_INTERVAL = "checkpoint.interval";
    public static final String KEY_CHECKPOINT_DIR = "checkpoint.dir";

    public static final String KEY_FLINK_PARALLELISM = "flink.parallelism";

}
