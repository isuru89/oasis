package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
final class RabbitDispatcher {

    private Connection connection;
    private Channel channel;

    private String exchangeName;

    private final ObjectMapper mapper = new ObjectMapper();

    void init(Configs configs) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configs.getStrReq(ConfigKeys.KEY_RABBIT_HOST));
        factory.setPort(Integer.parseInt(configs.getStr(ConfigKeys.KEY_RABBIT_PORT,
                String.valueOf(ConfigKeys.DEF_RABBIT_PORT))));
        factory.setVirtualHost(configs.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST,
                ConfigKeys.DEF_RABBIT_VIRTUAL_HOST));
        factory.setUsername(configs.getStrReq(ConfigKeys.KEY_RABBIT_SRVW_USERNAME));
        factory.setPassword(configs.getStrReq(ConfigKeys.KEY_RABBIT_SRVW_PASSWORD));

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

    void dispatch(long gameId, Map<String, Object> data) throws IOException {
        byte[] msg = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(UUID.randomUUID().toString())
                .build();

        String eventType = (String) data.get("type");
        String routingKey = String.format("game.%d.event.%s", gameId, eventType);
        channel.basicPublish(exchangeName, routingKey, properties, msg);
    }

    RabbitDispatcher() {}

}
