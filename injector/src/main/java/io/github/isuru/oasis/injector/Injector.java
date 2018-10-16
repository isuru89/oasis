package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.injector.scheduler.DailyScheduler;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
public class Injector {

    private static final boolean DURABLE = true;
    private static final boolean AUTO_ACK = false;
    private static final boolean AUTO_DEL = false;
    private static final boolean EXCLUSIVE = false;

    private Connection connection;
    private Channel channel;
    private IOasisDao dao;

    private void run() throws Exception {
        System.out.println(System.currentTimeMillis());
        //PropertyConfigurator.configure();
        Configs configs = initConfigs();
        DbProperties dbProps = DbProperties.fromProps(configs);

        dao = OasisDbFactory.create(dbProps);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dao.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

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

        // start scheduler
        startScheduler(configs, dao, gameIds);

        for (int gId : gameIds) {
            subscribeForGame(gId);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private static void startScheduler(Configs configs, IOasisDao dao, int[] gameIds) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }));

        for (int i = 0; i < gameIds.length; i++) {

            // add daily job
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("configs", configs);
            dataMap.put("dao", dao);
            dataMap.put("gameId", gameIds[i]);

            JobDetail jobDaily = JobBuilder.newJob(DailyScheduler.class)
                    .withIdentity("dailyScheduler", "OasisInjector")
                    .setJobData(dataMap)
                    .build();
            JobDetail jobWeekly = JobBuilder.newJob(DailyScheduler.class)
                    .withIdentity("weeklyScheduler", "OasisInjector")
                    .setJobData(dataMap)
                    .build();
            JobDetail jobMonthly = JobBuilder.newJob(DailyScheduler.class)
                    .withIdentity("monthlyScheduler", "OasisInjector")
                    .setJobData(dataMap)
                    .build();

            CronTrigger triggerDaily = TriggerBuilder.newTrigger()
                    .withIdentity("triggerDailyCron", "OasisInjector")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
                    .build();
            CronTrigger triggerWeekly = TriggerBuilder.newTrigger()
                    .withIdentity("triggerWeeklyCron", "OasisInjector")
                    .withSchedule(CronScheduleBuilder.cronSchedule("1 0 0 * * MON"))
                    .build();
            CronTrigger triggerMonthly = TriggerBuilder.newTrigger()
                    .withIdentity("triggerMonthlyCron", "OasisInjector")
                    .withSchedule(CronScheduleBuilder.cronSchedule("1 0 0 1 * ?"))
                    .build();

            scheduler.scheduleJob(jobDaily, triggerDaily);
            scheduler.scheduleJob(jobWeekly, triggerWeekly);
            scheduler.scheduleJob(jobMonthly, triggerMonthly);
        }

        scheduler.start();
    }

    private void subscribeForGame(int gameId) throws Exception {
        ContextInfo contextInfo = new ContextInfo();
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

        channel.basicConsume(pointsQ, AUTO_ACK, new PointConsumer(channel, dao, contextInfo));
        channel.basicConsume(msQ, AUTO_ACK, new MilestoneConsumer(channel, dao, contextInfo));
        channel.basicConsume(msStateQ, AUTO_ACK, new MilestoneStateConsumer(channel, dao, contextInfo));
        channel.basicConsume(badgesQ, AUTO_ACK, new BadgeConsumer(channel, dao, contextInfo));
        channel.basicConsume(challengesQ, AUTO_ACK, new ChallengeConsumer(channel, dao, contextInfo));
        channel.basicConsume(stateQ, AUTO_ACK, new StateConsumer(channel, dao, contextInfo));
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
