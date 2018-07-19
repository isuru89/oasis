package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.Oasis;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public class KafkaSender implements IBadgeHandler, IMilestoneHandler, IPointHandler, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);

    private String badgeStreamTopic;
    private String milestoneStreamTopic;
    private String pointsStreamTopic;
    private String milestoneStateStreamTopic;

    private KafkaProducer<Long, String> kafkaProducer;
    private ObjectMapper objectMapper;

    public void init(Properties kafkaProps, Oasis oasis) {
        String gameId = oasis.getId();
        badgeStreamTopic = gameId + "-badges";
        milestoneStreamTopic = gameId + "-milestones";
        pointsStreamTopic = gameId + "-points";
        milestoneStateStreamTopic = gameId + "-milestone_states";

        objectMapper = new ObjectMapper();
        kafkaProducer = new KafkaProducer<>(kafkaProps);
    }

    private KafkaSender() {}

    public static KafkaSender get() {
        return Holder.INSTANCE;
    }

    @Override
    public void badgeReceived(BadgeNotification badgeNotification) {
        try {
            long userId = badgeNotification.getUserId();

            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("badgeRuleId", badgeNotification.getRule().getId());
            map.put("badge", badgeNotification.getBadge());
            map.put("events", badgeNotification.getEvents());
            map.put("tag", badgeNotification.getTag());

            ProducerRecord<Long, String> record = new ProducerRecord<>(
                    badgeStreamTopic,
                    userId,
                    objectMapper.writeValueAsString(map));

            RecordMetadata recordMetadata = kafkaProducer.send(record).get();
            LOG.debug("Badge sent to kafka! [user={}, offset={}]", userId, recordMetadata.offset());

        } catch (Exception e) {
            LOG.error("Failed to send badge to kafka!", e);
        }
    }

    @Override
    public void milestoneReached(MilestoneNotification milestoneNotification) {
        try {
            long userId = milestoneNotification.getUserId();

            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("milestoneId", milestoneNotification.getMilestone().getId());
            map.put("level", milestoneNotification.getLevel());
            map.put("event", milestoneNotification.getEvent());

            ProducerRecord<Long, String> record = new ProducerRecord<>(
                    milestoneStreamTopic,
                    userId,
                    objectMapper.writeValueAsString(map));

            RecordMetadata recordMetadata = kafkaProducer.send(record).get();
            LOG.debug("Milestone reach sent to kafka! [user={}, offset={}]", userId, recordMetadata.offset());

        } catch (Exception e) {
            LOG.error("Failed to send milestone to kafka!", e);
        } finally {
            kafkaProducer.flush();
        }
    }

    @Override
    public void pointsScored(PointNotification pointNotification) {
        try {
            long userId = pointNotification.getUserId();

            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("pointId", pointNotification.getRule().getId());
            map.put("amount", pointNotification.getAmount());
            map.put("tag", pointNotification.getTag());
            map.put("events", pointNotification.getEvents());

            ProducerRecord<Long, String> record = new ProducerRecord<>(
                    pointsStreamTopic,
                    userId,
                    objectMapper.writeValueAsString(map));

            RecordMetadata recordMetadata = kafkaProducer.send(record).get();
            LOG.debug("Points sent to kafka! [user={}, offset={}]", userId, recordMetadata.offset());

        } catch (Exception e) {
            LOG.error("Failed to send point to kafka!", e);
        } finally {
            kafkaProducer.flush();
        }
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, double value) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("milestoneId", milestone.getId());
            map.put("value", value);
            map.put("value_i", null);

            ProducerRecord<Long, String> record = new ProducerRecord<>(
                    milestoneStateStreamTopic,
                    userId,
                    objectMapper.writeValueAsString(map));

            RecordMetadata recordMetadata = kafkaProducer.send(record).get();
            LOG.debug("Milestone state sent to kafka! [user={}, offset={}]", userId, recordMetadata.offset());

        } catch (Exception e) {
            LOG.error("Failed to send milestone state to kafka!", e);
        } finally {
            kafkaProducer.flush();
        }
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, long value) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("milestoneId", milestone.getId());
            map.put("value", null);
            map.put("value_i", value);

            ProducerRecord<Long, String> record = new ProducerRecord<>(
                    milestoneStateStreamTopic,
                    userId,
                    objectMapper.writeValueAsString(map));

            RecordMetadata recordMetadata = kafkaProducer.send(record).get();
            LOG.debug("Milestone state sent to kafka! [user={}, offset={}]", userId, recordMetadata.offset());

        } catch (Exception e) {
            LOG.error("Failed to send milestone state to kafka!", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (kafkaProducer != null) {
            kafkaProducer.flush();
            kafkaProducer.close(30L, TimeUnit.SECONDS);
        }
    }

    private static class Holder {
        private static final KafkaSender INSTANCE = new KafkaSender();
    }

}
