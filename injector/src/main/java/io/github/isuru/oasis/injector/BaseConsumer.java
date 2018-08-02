package io.github.isuru.oasis.injector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.isuru.oasis.db.IOasisDao;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public abstract class BaseConsumer<T> extends DefaultConsumer {

    static final ObjectMapper MAPPER = new ObjectMapper();

    protected IOasisDao dao;
    private Class<T> clz;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public BaseConsumer(Channel channel, IOasisDao dao, Class<T> clz) {
        super(channel);
        this.dao = dao;
        this.clz = clz;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            T message = MAPPER.readValue(body, clz);
            if (handle(message)) {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract boolean handle(T msg);
}
