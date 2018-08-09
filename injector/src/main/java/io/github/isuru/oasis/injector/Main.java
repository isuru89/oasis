package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;

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
        DbProperties dbProps = new DbProperties("oasis-injector");
        dbProps.setUrl("jdbc:mysql://localhost/oasis");
        dbProps.setUsername("isuru");
        dbProps.setPassword("isuru");
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

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(100);

        channel.queueDeclare("game.o.points", DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare("game.o.milestones", DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare("game.o.milestonestates", DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare("game.o.badges", DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare("game.o.challenges", DURABLE, EXCLUSIVE, AUTO_DEL, null);

        channel.basicConsume("game.o.points", AUTO_ACK, new PointConsumer(channel, dao));
        channel.basicConsume("game.o.milestones", AUTO_ACK, new MilestoneConsumer(channel, dao));
        channel.basicConsume("game.o.milestonestates", AUTO_ACK, new MilestoneStateConsumer(channel, dao));
        channel.basicConsume("game.o.badges", AUTO_ACK, new BadgeConsumer(channel, dao));
        channel.basicConsume("game.o.challenges", AUTO_ACK, new ChallengeConsumer(channel, dao));

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

}
