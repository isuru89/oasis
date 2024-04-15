package io.github.oasis.eventsapi.configs;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.messages.EngineMessage;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReactiveKafkaConfigs {

    @Bean
    public ReactiveKafkaProducerTemplate<String, EngineMessage> createStreamConfig(OasisConfigs configs) {
        var props = new HashMap<String, Object>();

        var brokerUrls = configs.get("oasis.eventstream.configs.brokerUrls", null);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }

}
