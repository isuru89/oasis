package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;

import java.io.File;

/**
 * @author iweerarathna
 */
public class Main {


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

        channel.queueDeclare("game.o.points", false, false, false, null);
        channel.queueDeclare("game.o.milestones", false, false, false, null);
        channel.queueDeclare("game.o.milestonestates", false, false, false, null);
        channel.queueDeclare("game.o.badges", false, false, false, null);
        //channel.queueDeclare("game.o.challenges", true, false, false, null);

        channel.basicConsume("game.o.points", false, new PointConsumer(channel, dao));
        channel.basicConsume("game.o.milestones", false, new MilestoneConsumer(channel, dao));
        channel.basicConsume("game.o.milestonestates", false, new MilestoneStateConsumer(channel, dao));
        channel.basicConsume("game.o.badges", false, new BadgeConsumer(channel, dao));

    }

}
