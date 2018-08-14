package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.configs.ConfigKeys;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
public class Main {

    private static final boolean DURABLE = true;
    private static final boolean AUTO_ACK = false;
    private static final boolean AUTO_DEL = false;
    private static final boolean EXCLUSIVE = false;

    public static void main(String[] args) throws Exception {
        //PropertyConfigurator.configure();
        DbProperties dbProps = new DbProperties("oasis-injector");
        dbProps.setUrl("jdbc:mariadb://localhost/oasis");
        dbProps.setUsername("isuru");
        dbProps.setPassword("isuru");
        //dbProps.setUseTemplateEngine(false);
        dbProps.setQueryLocation(new File("./scripts/db").getAbsolutePath());

        IOasisDao dao = OasisDbFactory.create(dbProps);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dao.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("reader");
        factory.setPassword("reader");
        factory.setVirtualHost("oasis");
        factory.setAutomaticRecoveryEnabled(true);
        factory.useNio();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(100);

        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setGameId(1);

        String pointsQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_POINTS_SINK, contextInfo.getGameId());
        String badgesQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_BADGES_SINK, contextInfo.getGameId());
        String msQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK, contextInfo.getGameId());
        String msStateQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK, contextInfo.getGameId());
        String challengesQ = replaceQ(ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK, contextInfo.getGameId());

        channel.queueDeclare(pointsQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(msQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(msStateQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(badgesQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(challengesQ, DURABLE, EXCLUSIVE, AUTO_DEL, null);

        channel.basicConsume(pointsQ, AUTO_ACK, new PointConsumer(channel, dao, contextInfo));
        channel.basicConsume(msQ, AUTO_ACK, new MilestoneConsumer(channel, dao, contextInfo));
        channel.basicConsume(msStateQ, AUTO_ACK, new MilestoneStateConsumer(channel, dao, contextInfo));
        channel.basicConsume(badgesQ, AUTO_ACK, new BadgeConsumer(channel, dao, contextInfo));
        channel.basicConsume(challengesQ, AUTO_ACK, new ChallengeConsumer(channel, dao, contextInfo));

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

    private static String replaceQ(String name, long id) {
        return name.replace("{gid}", String.valueOf(id));
    }

}
