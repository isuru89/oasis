package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.model.ConfigKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
public final class RabbitDispatcher {

    private Connection connection;
    private Channel channel;

    private String exchangeName;

    private final ObjectMapper mapper = new ObjectMapper();

    public void init() throws IOException, TimeoutException {
        Configs configs = Configs.get();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configs.getStrReq(ConfigKeys.KEY_RABBIT_HOST));
        factory.setPort(Integer.parseInt(configs.getStr(ConfigKeys.KEY_RABBIT_PORT,
                String.valueOf(ConfigKeys.DEF_RABBIT_PORT))));
        factory.setVirtualHost(configs.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST,
                ConfigKeys.DEF_RABBIT_VIRTUAL_HOST));
        factory.setUsername(configs.getStrReq(ConfigKeys.KEY_RABBIT_USERNAME));
        factory.setPassword(configs.getStrReq(ConfigKeys.KEY_RABBIT_PASSWORD));

        connection = factory.newConnection();
        channel = connection.createChannel();

        exchangeName = configs.getStrReq(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_NAME);
        String exchangeType = configs.getStr(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_TYPE,
                ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_TYPE);
        boolean durable = configs.getBool(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_DURABLE,
                ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_DURABLE);

        channel.exchangeDeclare(exchangeName, exchangeType, durable, false, null);

        // register shutdown hook
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

    public void dispatch(Map<String, Object> data) throws IOException {
        byte[] msg = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(UUID.randomUUID().toString())
                .build();

        String eventType = (String) data.get("type");
        String routingKey = "game.event." + eventType;
        channel.basicPublish(exchangeName, routingKey, properties, msg);
    }

    public static RabbitDispatcher get() {
        return Holder.INSTANCE;
    }

    private RabbitDispatcher() {}

    private static class Holder {
        private static final RabbitDispatcher INSTANCE = new RabbitDispatcher();
    }

}
