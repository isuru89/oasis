package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
public class Injector {

    private static final Logger LOG = LoggerFactory.getLogger(Injector.class);

    private static final boolean DURABLE = true;
    private static final boolean AUTO_ACK = false;
    private static final boolean AUTO_DEL = false;
    private static final boolean EXCLUSIVE = false;
    private static final String OASIS_INJECTOR = "OasisInjector";

    private Connection connection;
    private Channel channel;
    private IOasisDao dao;

    private void run() throws Exception {
        LOG.info("Starting injector...");

        Configs configs = initConfigs();
        DbProperties dbProps = DbProperties.fromProps(configs);

        LOG.debug("Initializing database connection...");
        dao = OasisDbFactory.create(dbProps);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dao.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }));

        LOG.debug("Initializing rabbitmq connection...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(OasisUtils.getEnvOr(EnvKeys.OASIS_RABBIT_HOST,
                configs.getStr(ConfigKeys.KEY_RABBIT_HOST, "localhost")));
        factory.setPort(configs.getInt(ConfigKeys.KEY_RABBIT_PORT, ConfigKeys.DEF_RABBIT_PORT));
        factory.setUsername(configs.getStr(ConfigKeys.KEY_RABBIT_INJ_USERNAME, "injector"));
        factory.setPassword(configs.getStr(ConfigKeys.KEY_RABBIT_INJ_PASSWORD, "injector"));
        factory.setVirtualHost(configs.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST, ConfigKeys.DEF_RABBIT_VIRTUAL_HOST));
        factory.setAutomaticRecoveryEnabled(true);
        factory.useNio();

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(Integer.parseInt(
                OasisUtils.getEnvOr(EnvKeys.OASIS_INJECTOR_PREFETCH_SIZE, "100")));

        int[] gameIds = {1}; // @TODO change game id

        for (int gId : gameIds) {
            subscribeForGame(gId);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                LOG.error(e.getMessage(), e);
            }
            try {
                connection.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }));
    }

    private void subscribeForGame(int gameId) throws Exception {
        ContextInfo contextInfo = new ContextInfo(7);
        contextInfo.setGameId(gameId);

        String pointsQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_POINTS_SINK, contextInfo.getGameId());
        String badgesQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_BADGES_SINK, contextInfo.getGameId());
        String msQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK, contextInfo.getGameId());
        String msStateQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK, contextInfo.getGameId());
        String challengesQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK, contextInfo.getGameId());
        String stateQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_STATES_SINK, contextInfo.getGameId());

        channel.queueDeclare(pointsQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(msQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(msStateQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(badgesQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(challengesQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(stateQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);

        PointConsumer pointConsumer = new PointConsumer(channel, dao, contextInfo);
        MilestoneConsumer milestoneConsumer = new MilestoneConsumer(channel, dao, contextInfo);
        MilestoneStateConsumer milestoneStateConsumer = new MilestoneStateConsumer(channel, dao, contextInfo);
        BadgeConsumer badgeConsumer = new BadgeConsumer(channel, dao, contextInfo);
        ChallengeConsumer challengeConsumer = new ChallengeConsumer(channel, dao, contextInfo);
        StateConsumer stateConsumer = new StateConsumer(channel, dao, contextInfo);

        registerShutdownHook(pointConsumer);
        registerShutdownHook(milestoneConsumer);
        registerShutdownHook(milestoneStateConsumer);
        registerShutdownHook(badgeConsumer);
        registerShutdownHook(challengeConsumer);
        registerShutdownHook(stateConsumer);

        channel.basicConsume(pointsQ, AUTO_ACK, pointConsumer);
        channel.basicConsume(msQ, AUTO_ACK, milestoneConsumer);
        channel.basicConsume(msStateQ, AUTO_ACK, milestoneStateConsumer);
        channel.basicConsume(badgesQ, AUTO_ACK, badgeConsumer);
        channel.basicConsume(challengesQ, AUTO_ACK, challengeConsumer);
        channel.basicConsume(stateQ, AUTO_ACK, stateConsumer);
    }

    private void registerShutdownHook(Closeable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.error("Error closing {}!", closeable.getClass().getName());
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        new Injector().run();
    }

    private Injector() {}

    private static String replaceQ(String name, long id) {
        return name.replace("{gid}", String.valueOf(id));
    }

    private Configs initConfigs() throws IOException {
        String configDirStr = OasisUtils.getEnvOr(EnvKeys.OASIS_CONFIG_DIR, "./configs");
        LOG.debug("Loading configurations from {}", configDirStr);

        File configDir = new File(configDirStr);
        if (!configDir.exists()) {
            throw new RuntimeException("Configuration directory is not found! '" + configDirStr + "'");
        }

        File[] files = configDir.listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("No configuration files found inside '" + configDirStr + "' dir!");
        }

        Configs configs = Configs.create();
        int count = 0;
        for (File file : files) {
            if (file.getName().endsWith(".properties")) {
                count++;
                try (InputStream inputStream = new FileInputStream(file)) {
                    configs.init(inputStream);
                }
            }
        }

        if (count == 0) {
            throw new RuntimeException("No configuration property files found inside '" + configDirStr + "' dir!");
        }
        return configs.initWithSysProps();
    }
}
