package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.model.ConfigKeys;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.util.Properties;

/**
 * @author iweerarathna
 */
public class RabbitUtils {


    public static RMQConnectionConfig createRabbitConfig(Properties gameProps) {
        return new RMQConnectionConfig.Builder()
                .setHost(gameProps.getProperty(ConfigKeys.KEY_RABBIT_HOST, "localhost"))
                .setPort(Integer.parseInt(gameProps.getProperty(ConfigKeys.KEY_RABBIT_PORT, "5672")))
                .setVirtualHost(gameProps.getProperty(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST, "oasis"))
                .setUserName(gameProps.getProperty(ConfigKeys.KEY_RABBIT_USERNAME))
                .setPassword(gameProps.getProperty(ConfigKeys.KEY_RABBIT_PASSWORD))
                .build();
    }

}
