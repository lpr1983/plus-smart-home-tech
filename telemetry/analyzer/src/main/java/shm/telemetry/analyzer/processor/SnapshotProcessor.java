package shm.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import shm.telemetry.analyzer.kafka.KafkaProperties;
import shm.telemetry.analyzer.serialization.SensorsSnapshotAvroDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class SnapshotProcessor {
    private final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);
    private final KafkaProperties kafkaProperties;

    public SnapshotProcessor(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public void run() {
        List<String> consumerTopics = List.of(kafkaProperties.consumer().snapshotProcessor().topic());
        Duration pollTimeout = Duration.ofMillis(kafkaProperties.consumer().pollTimeout());

        try (
                KafkaConsumer<Void, SensorsSnapshotAvro> consumer = createConsumer();
        ) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(consumerTopics);

            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
                    log.debug("polled SensorsSnapshotAvro {}", record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignore) {
            log.info("Завершение работы SnapshotProcessor");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки данных SnapshotProcessor", e);
            throw new RuntimeException("Critical error in SnapshotProcessor", e);
        }

    }

    private KafkaConsumer<Void, SensorsSnapshotAvro> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.consumer().snapshotProcessor().clientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.consumer().snapshotProcessor().groupId());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.server());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotAvroDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Отключение автокомита
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // При вхождении в новую группу забирать с начала

        return new KafkaConsumer<>(properties);
    }

}
