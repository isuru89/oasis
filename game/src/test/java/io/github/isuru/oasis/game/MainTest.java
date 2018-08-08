package io.github.isuru.oasis.game;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.game.persist.DbOutputHandler;
import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.process.sources.CsvEventSource;
import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

class MainTest {

    @Test
    void testCreateSource() throws FileNotFoundException {
        {
            Properties properties = new Properties();
            properties.put(Constants.KEY_SOURCE_TYPE, "file");
            properties.put(Constants.KEY_SOURCE_FILE, "../non/existing/file");
            try {
                Main.createSource(Configs.from(properties));
                Assertions.fail("File source creation should fail when source does not exist!");
            } catch (FileNotFoundException e) {
                // ok
            }
        }
        {
            Properties properties = new Properties();
            properties.put(Constants.KEY_SOURCE_TYPE, "file");
            properties.put(Constants.KEY_SOURCE_FILE, "../scripts/examples/input.csv");

            SourceFunction<Event> source = Main.createSource(Configs.from(properties));
            Assertions.assertTrue(source instanceof CsvEventSource);
        }

        {
            Properties properties = new Properties();
            properties.put(Constants.KEY_SOURCE_TYPE, "kafka");
            properties.put(Constants.KEY_KAFKA_SOURCE_TOPIC, "game-events");
            properties.put(Constants.KEY_KAFKA_HOST, "localhost:9092");

            SourceFunction<Event> source = Main.createSource(Configs.from(properties));
            Assertions.assertTrue(source instanceof FlinkKafkaConsumer011);
        }
    }

    @Test
    void testDbPropertiesCreation() throws Exception {
        File scriptsDir = deriveScriptsDir();

        Properties properties = new Properties();
        properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
        properties.put(Constants.KEY_JDBC_URL, "jdbc:mysql://localhost/oasis");
        properties.put(Constants.KEY_JDBC_USERNAME, "isuru");
        properties.put(Constants.KEY_JDBC_PASSWORD, "isuru");
        properties.put(Constants.KEY_DB_SCRIPTS_DIR, scriptsDir.getAbsolutePath());

        {
            DbProperties configs = Main.createConfigs(Configs.from(properties));
            Assertions.assertNotNull(configs);
            Assertions.assertEquals(configs.getDaoName(), "testing");
            Assertions.assertEquals(configs.getUsername(), "isuru");
            Assertions.assertEquals(configs.getPassword(), "isuru");
            Assertions.assertEquals(configs.getUrl(), "jdbc:mysql://localhost/oasis");
            Assertions.assertEquals(configs.getQueryLocation(), scriptsDir.getAbsolutePath());
        }
        {
            properties.remove(Constants.KEY_JDBC_PASSWORD);
            DbProperties configs = Main.createConfigs(Configs.from(properties));
            Assertions.assertNull(configs.getPassword());
            properties.put(Constants.KEY_JDBC_PASSWORD, "");

            configs = Main.createConfigs(Configs.from(properties));
            Assertions.assertNotNull(configs.getPassword());
            Assertions.assertEquals(configs.getPassword(), "");
        }
        {
            properties.put(Constants.KEY_DB_SCRIPTS_DIR, "../hello/non/existing");
            try {
                Main.createConfigs(Configs.from(properties));
                Assertions.fail("Non existing script dir should fail!");
            } catch (FileNotFoundException ex) {
                // ok
            }
        }
    }

    @Test
    void testOutputKafkaSink() throws Exception {
        {
            Properties properties = new Properties();
            properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
            properties.put(Constants.KEY_OUTPUT_TYPE, "kafka");

            properties.put(Constants.KEY_KAFKA_HOST, "localhost:9092");

            OasisExecution execution = Main.createOutputHandler(Configs.from(properties), new OasisExecution());
            Assertions.assertNotNull(execution);

            OasisKafkaSink kafkaSink = (OasisKafkaSink) execution.getKafkaSink();
            Assertions.assertNotNull(kafkaSink);
            Assertions.assertNull(execution.getOutputHandler());

            Assertions.assertNotNull(kafkaSink.getKafkaHost());
            Assertions.assertNotNull(kafkaSink.getTopicBadges());
            Assertions.assertNotNull(kafkaSink.getTopicChallengeWinners());
            Assertions.assertNotNull(kafkaSink.getTopicMilestones());
            Assertions.assertNotNull(kafkaSink.getTopicMilestoneStates());
            Assertions.assertNotNull(kafkaSink.getTopicPoints());
            Assertions.assertNull(kafkaSink.getProducerConfigs());
        }

        Properties properties = new Properties();
        properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
        properties.put(Constants.KEY_OUTPUT_TYPE, "kafka");

        properties.put(Constants.KEY_KAFKA_HOST, "localhost:9092");

        OasisChallengeExecution execution = Main.createOutputHandler(Configs.from(properties), new OasisChallengeExecution());
        Assertions.assertNotNull(execution);

        OasisKafkaSink kafkaSink = (OasisKafkaSink) execution.getOutputSink();
        Assertions.assertNotNull(kafkaSink);
        Assertions.assertNull(execution.getOutputHandler());

        Assertions.assertNotNull(kafkaSink.getKafkaHost());
        Assertions.assertNotNull(kafkaSink.getTopicBadges());
        Assertions.assertNotNull(kafkaSink.getTopicChallengeWinners());
        Assertions.assertNotNull(kafkaSink.getTopicMilestones());
        Assertions.assertNotNull(kafkaSink.getTopicMilestoneStates());
        Assertions.assertNotNull(kafkaSink.getTopicPoints());
        Assertions.assertNull(kafkaSink.getProducerConfigs());
    }

    @Test
    void testOutputDbCreation() throws Exception {
        {
            Properties properties = new Properties();
            properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
            properties.put(Constants.KEY_OUTPUT_TYPE, "db");
            properties.put("db.scripts.dir", "../scripts/db");
            properties.put("jdbc.url", "jdbc:h2:./test");
            properties.put("jdbc.username", "");
            properties.put("jdbc.password", "");

            OasisExecution execution = Main.createOutputHandler(Configs.from(properties), new OasisExecution());
            Assertions.assertNotNull(execution);

            IOutputHandler outputHandler = execution.getOutputHandler();
            Assertions.assertNotNull(outputHandler);
            Assertions.assertNull(execution.getKafkaSink());

            Assertions.assertTrue(outputHandler instanceof DbOutputHandler);
            DbOutputHandler dbOutputHandler = (DbOutputHandler) outputHandler;
            Assertions.assertEquals(dbOutputHandler.getDbRef(), "testing");
        }

        Properties properties = new Properties();
        properties.put(Constants.KEY_JDBC_INSTANCE, "testing");
        properties.put(Constants.KEY_OUTPUT_TYPE, "db");
        OasisChallengeExecution challengeExecution = Main.createOutputHandler(Configs.from(properties), new OasisChallengeExecution());
        Assertions.assertNotNull(challengeExecution);

        IOutputHandler outputHandler = challengeExecution.getOutputHandler();
        Assertions.assertNotNull(outputHandler);
        Assertions.assertNull(challengeExecution.getOutputSink());

        Assertions.assertTrue(outputHandler instanceof DbOutputHandler);
        DbOutputHandler dbOutputHandler = (DbOutputHandler) outputHandler;
        Assertions.assertEquals(dbOutputHandler.getDbRef(), "testing");
    }

    private File deriveScriptsDir() throws IOException {
        File dir = new File("./scripts/db");
        if (dir.exists()) {
            return dir.getCanonicalFile();
        } else {
            dir = new File("../scripts/db");
            if (dir.exists()) {
                return dir.getCanonicalFile();
            }
        }
        throw new RuntimeException("Scripts dir cannot be found!");
    }
}
