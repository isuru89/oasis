package io.github.isuru.oasis.injector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public abstract class BaseConsumer<T> extends DefaultConsumer implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConsumer.class);

    public static final ObjectMapper MAPPER = new ObjectMapper();

    protected IOasisDao dao;
    private Class<T> clz;
    ContextInfo contextInfo;

    private final BufferedRecords buffer;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    BaseConsumer(Channel channel, IOasisDao dao, Class<T> clz, ContextInfo context) {
        this(channel, dao, clz, context, true);
    }

    BaseConsumer(Channel channel, IOasisDao dao, Class<T> clz, ContextInfo context, boolean buffered) {
        super(channel);
        this.dao = dao;
        this.clz = clz;
        this.contextInfo = context;

        if (buffered) {
            this.buffer = new BufferedRecords(this::pumpRecordsToDb);
            this.buffer.init(contextInfo.getPool());
        } else {
            this.buffer = null;
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        try {
            LOG.debug("Message received from: {} [{}]", envelope.getRoutingKey(), envelope.getDeliveryTag());
            T message = MAPPER.readValue(body, clz);
            Map<String, Object> serializedData = handle(message);
            BufferedRecords.ElementRecord record = new BufferedRecords.ElementRecord(serializedData,
                    envelope.getDeliveryTag());
            buffer.push(record);
            contextInfo.getInterceptor().consume(message);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public abstract Map<String, Object> handle(T msg);

    private void pumpRecordsToDb(List<BufferedRecords.ElementRecord> recordList) {
        flushRecords(recordList, getInsertScriptName());
    }

    void flushRecords(List<BufferedRecords.ElementRecord> recordList,
                      String scriptName) {
        try {
            List<Map<String, Object>> maps = recordList.stream()
                    .map(BufferedRecords.ElementRecord::getData)
                    .collect(Collectors.toList());
            dao.executeBatchInsert(scriptName, maps);

            List<Long> tags = recordList.stream()
                    .map(BufferedRecords.ElementRecord::getDeliveryTag)
                    .collect(Collectors.toList());
            for (long tag : tags) {
                getChannel().basicAck(tag, false);
            }
            LOG.debug("{} message ack completed for #{} messages.", clz.getSimpleName(), tags.size());

        } catch (DbException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("Error while sending ack for {} messages!", clz.getSimpleName(), e);
        }
    }

    public abstract String getInsertScriptName();

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
        }
    }
}
