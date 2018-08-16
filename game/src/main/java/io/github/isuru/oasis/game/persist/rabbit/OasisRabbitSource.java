package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class OasisRabbitSource extends RMQSource<Event> {

    private Configs props;

    public OasisRabbitSource(Configs gameProps,
                             RMQConnectionConfig rmqConnectionConfig,
                             String queueName,
                             boolean usesCorrelationId,
                             DeserializationSchema<Event> deserializationSchema) {
        super(rmqConnectionConfig, queueName, usesCorrelationId, deserializationSchema);

        props = gameProps;
    }

    @Override
    protected void setupQueue() throws IOException {
        String exchangeName = props.getStrReq(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_NAME);
        String exchangeType = props.getStr(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_TYPE,
                ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_TYPE);
        boolean durable = props.getBool(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_DURABLE,
                        ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_DURABLE);

        channel.exchangeDeclare(exchangeName, exchangeType, durable, false, null);
        channel.queueDeclare(queueName, durable, false, false, null);

        channel.queueBind(queueName, exchangeName, "");
    }
}
