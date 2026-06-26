package shm.telemetry.aggregator.kafka;

import avro.serialization.GeneralAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaConsumer<Void, SensorEventAvro> kafkaConsumer(@Value("${kafka.server}") String kafkaServer,
                                                              @Value("${kafka.consumer.client-id}") String clientId,
                                                              @Value("${kafka.consumer.group-id}") String groupId
    ) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventAvroDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<Void, SensorEventAvro> consumer = new KafkaConsumer<>(properties);

        return consumer;
    }

    public static class SensorEventAvroDeserializer extends GeneralAvroDeserializer<SensorEventAvro> {

        public SensorEventAvroDeserializer() {
            super(SensorEventAvro.getClassSchema());
        }

    }

}