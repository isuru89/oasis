package io.github.isuru.oasis.game.persist.rabbit;

import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.util.Properties;

/**
 * @author iweerarathna
 */
public class RabbitUtils {


    public static RMQConnectionConfig createRabbitConfig(Properties gameProps) {
        return new RMQConnectionConfig.Builder()
                .setHost(gameProps.getProperty("rabbit.host", "localhost"))
                .setPort(Integer.parseInt(gameProps.getProperty("rabbit.port", "5672")))
                .setVirtualHost(gameProps.getProperty("rabbit.virtualhost", "oasis"))
                .setUserName(gameProps.getProperty("rabbit.username"))
                .setPassword(gameProps.getProperty("rabbit.password"))
                .build();
    }

}
