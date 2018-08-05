package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.model.ConfigKeys;
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

    private boolean durable;

    private static final Logger LOG = LoggerFactory.getLogger(RMQOasisSink.class);

    RMQOasisSink(RMQConnectionConfig rmqConnectionConfig,
                 String queueName,
                 SerializationSchema<IN> schema,
                 Properties gameProps) {
        super(rmqConnectionConfig, queueName, schema);

        this.durable = Boolean.parseBoolean(gameProps.getProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUTPUT_DURABLE, "true"));
    }

    @Override
    protected void setupQueue() throws IOException {
        super.channel.queueDeclare(this.queueName, this.durable, false, false, null);
    }

    @Override
    public void invoke(IN value) {
        try {
            byte[] msg = this.schema.serialize(value);
            this.channel.basicPublish("", this.queueName, null, msg);
        } catch (IOException var3) {
            LOG.error("Cannot send RMQ message {} at {}", new Object[]{this.queueName, var3});
        }
    }
}
