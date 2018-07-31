package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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

    private String queueName;

    private final ObjectMapper mapper = new ObjectMapper();

    public void init() throws IOException, TimeoutException {
        Configs configs = Configs.get();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configs.getStrReq("rabbit.host"));
        factory.setPort(Integer.parseInt(configs.getStr("rabbit.port", "5072")));
        factory.setVirtualHost(configs.getStr("rabbit.virtualhost", "oasis"));
        factory.setUsername(configs.getStrReq("rabbit.username"));
        factory.setPassword(configs.getStrReq("rabbit.password"));

        connection = factory.newConnection();
        channel = connection.createChannel();

        queueName = configs.getStrReq("rabbit.queue.src");

        channel.queueDeclare(queueName, true, false, false, null);

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
        channel.basicPublish("", queueName, properties, msg);
    }

    public static RabbitDispatcher get() {
        return Holder.INSTANCE;
    }

    private RabbitDispatcher() {}

    private static class Holder {
        private static final RabbitDispatcher INSTANCE = new RabbitDispatcher();
    }

}
