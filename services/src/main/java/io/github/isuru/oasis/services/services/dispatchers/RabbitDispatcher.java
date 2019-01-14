package io.github.isuru.oasis.services.services.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.configs.RabbitConfigurations;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
@Component("dispatcherRabbit")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class RabbitDispatcher implements IEventDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitDispatcher.class);

    private Connection connection;
    private Channel channel;

    private String exchangeName;

    private final ObjectMapper mapper = new ObjectMapper();

    private final RabbitConfigurations rabbitConfigurations;

    @Autowired
    public RabbitDispatcher(RabbitConfigurations rabbitConfigurations) {
        this.rabbitConfigurations = rabbitConfigurations;
    }

    public void init(Configs configs) throws IOException {
        try {
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
        } catch (TimeoutException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void init() throws IOException {
        try {
            RabbitConfigurations configs = rabbitConfigurations;
            System.out.println("RABBIT HOST: " + configs.getHost());
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(configs.getHost());
            factory.setPort(configs.getPort());
            factory.setVirtualHost(configs.getVirtualHost());
            factory.setUsername(configs.getUsername());
            factory.setPassword(configs.getPassword());

            connection = factory.newConnection();
            channel = connection.createChannel();

            exchangeName = configs.getSourceExchangeName();
            String exchangeType = configs.getSourceExchangeType();
            boolean durable = configs.isSourceExchangeDurable();

            channel.exchangeDeclare(exchangeName, exchangeType, durable, false, null);

        } catch (TimeoutException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void dispatch(long gameId, Map<String, Object> data) throws IOException {
        byte[] msg = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(UUID.randomUUID().toString())
                .build();

        String eventType = (String) data.get("type");
        String routingKey = String.format("game.%d.event.%s", gameId, eventType);
        channel.basicPublish(exchangeName, routingKey, properties, msg);
    }

    @Override
    public void close() {
        LOG.info("Closing rabbit event dispatcher...");
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
