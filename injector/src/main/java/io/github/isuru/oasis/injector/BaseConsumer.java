package io.github.isuru.oasis.injector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.isuru.oasis.model.db.IOasisDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public abstract class BaseConsumer<T> extends DefaultConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConsumer.class);

    public static final ObjectMapper MAPPER = new ObjectMapper();

    protected IOasisDao dao;
    private Class<T> clz;
    protected ContextInfo contextInfo;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public BaseConsumer(Channel channel, IOasisDao dao, Class<T> clz, ContextInfo context) {
        super(channel);
        this.dao = dao;
        this.clz = clz;
        this.contextInfo = context;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            LOGGER.debug("Message received from: {} [{}]", envelope.getRoutingKey(), envelope.getDeliveryTag());
            T message = MAPPER.readValue(body, clz);
            if (handle(message)) {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                LOGGER.debug("Message ack completed. [{}]", envelope.getDeliveryTag());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract boolean handle(T msg);
}
