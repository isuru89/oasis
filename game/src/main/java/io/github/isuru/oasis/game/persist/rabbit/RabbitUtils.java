package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * @author iweerarathna
 */
public class RabbitUtils {


    public static RMQConnectionConfig createRabbitConfig(Configs gameProps) {
        return new RMQConnectionConfig.Builder()
                .setHost(OasisUtils.getEnvOr(EnvKeys.OASIS_RABBIT_HOST,
                                gameProps.getStr(ConfigKeys.KEY_RABBIT_HOST, "localhost")))
                .setPort(gameProps.getInt(ConfigKeys.KEY_RABBIT_PORT, ConfigKeys.DEF_RABBIT_PORT))
                .setVirtualHost(gameProps.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST, ConfigKeys.DEF_RABBIT_VIRTUAL_HOST))
                .setUserName(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_USERNAME))
                .setPassword(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_PASSWORD))
                .build();
    }

}
