package io.github.isuru.oasis.game.persist.rabbit;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author iweerarathna
 */
class RMQOasisSink<IN> extends RMQSink<IN> {

    private String exchangeName;
    private String exchangeType;
    private boolean durable;

    private static final Logger LOG = LoggerFactory.getLogger(RMQOasisSink.class);

    public RMQOasisSink(RMQConnectionConfig rmqConnectionConfig,
                        String queueName,
                        SerializationSchema<IN> schema,
                        Properties gameProps) {
        super(rmqConnectionConfig, queueName, schema);

        this.exchangeName = gameProps.getProperty("rabbit.msg.exchange");
        this.exchangeType = gameProps.getProperty("rabbit.msg.exchange.type", "direct");
        this.durable = Boolean.parseBoolean(gameProps.getProperty("rabbit.msg.durable", "true"));
    }

    @Override
    protected void setupQueue() throws IOException {
        super.channel.exchangeDeclare(this.exchangeName, this.exchangeType, this.durable, false, false, null);
        super.channel.queueDeclare(this.queueName, this.durable, false, false, null);

        super.channel.queueBind(this.queueName, this.exchangeName, this.queueName);
    }

    @Override
    public void invoke(IN value) {
        try {
            byte[] msg = this.schema.serialize(value);
            this.channel.basicPublish(this.exchangeName, this.queueName, null, msg);
        } catch (IOException var3) {
            LOG.error("Cannot send RMQ message {} at {}", new Object[]{this.queueName, var3});
        }
    }
}
