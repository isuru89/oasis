package io.github.isuru.oasis.services.services.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
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
    public RabbitDispatcher(OasisConfigurations oasisConfigurations) {
        this.rabbitConfigurations = oasisConfigurations.getRabbit();
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
