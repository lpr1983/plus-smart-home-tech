package shm.telemetry.aggregator;

import avro.serialization.GeneralAvroSerializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import shm.telemetry.aggregator.kafka.KafkaProperties;
import shm.telemetry.aggregator.serialization.SensorEventAvroDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

@Component
public class AggregationStarter {
    private final Logger log = LoggerFactory.getLogger(AggregationStarter.class);
    private final KafkaProperties kafkaProperties;
    private final Map<String, SensorsSnapshotAvro> allSnapshots;

    public AggregationStarter(KafkaProperties properties) {
        this.kafkaProperties = properties;
        this.allSnapshots = new HashMap<>();
    }

    public void work() {
        final List<String> consumerTopics = List.of(kafkaProperties.consumer().topic());

        final String producerTopic = kafkaProperties.producer().topic();

        final Duration pollTimeout = Duration.ofMillis(
                kafkaProperties.consumer().consumeAttemptTimeoutMs());

        try (
                KafkaConsumer<Void, SensorEventAvro> consumer = createConsumer();
                Producer<Void, SpecificRecordBase> producer = createProducer();
        ) {

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(consumerTopics);

            while (true) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(pollTimeout);

                List<SensorsSnapshotAvro> updatedSnapshots = processRecordsAndReturnUpdatedSnapshots(records);

                List<Future<RecordMetadata>> results = new ArrayList<>();

                for (SensorsSnapshotAvro snapshotAvro : updatedSnapshots) {
                    ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(producerTopic, snapshotAvro);
                    results.add(producer.send(record));
                }

                for (Future<RecordMetadata> f : results) {
                    f.get();
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (WakeupException ignore) {
            log.info("Завершение работы");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки данных work()", e);
            throw new RuntimeException("Critical error in aggregator", e);
        }
    }

    private List<SensorsSnapshotAvro> processRecordsAndReturnUpdatedSnapshots
            (ConsumerRecords<Void, SensorEventAvro> records) {
        Set<String> hubsIds = new HashSet<>();

        for (ConsumerRecord<Void, SensorEventAvro> record : records) {
            log.debug("Polled object={}", record.value());

            SensorEventAvro sensorEventAvro = record.value();

            boolean updated = updateState(sensorEventAvro);
            if (updated) {
                hubsIds.add(sensorEventAvro.getHubId());
            }
        }

        return hubsIds.stream().map(i -> allSnapshots.get(i))
                .toList();
    }

    private KafkaConsumer<Void, SensorEventAvro> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.consumer().clientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.consumer().groupId());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.server());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventAvroDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Отключение автокомита
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // При вхождении в новую группу забирать с начала

        return new KafkaConsumer<>(properties);
    }

    private Producer<Void, SpecificRecordBase> createProducer() {
        Properties config = new Properties();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.server());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        return new KafkaProducer<>(config);
    }

    private void checkAndSetSnapshotTime(SensorsSnapshotAvro snapshot, Instant timestamp) {
        if (timestamp == null || snapshot == null) {
            return;
        }
        Instant snapshotTime = snapshot.getTimestamp();
        if (snapshotTime == null || timestamp.isAfter(snapshot.getTimestamp())) {
            snapshot.setTimestamp(timestamp);
        }
    }

    private boolean updateState(SensorEventAvro sensorEvent) {

        String hubId = sensorEvent.getHubId();
        SensorsSnapshotAvro hubSnapshot = allSnapshots.get(hubId);

        String deviceId = sensorEvent.getId();
        SpecificRecordBase payload = (SpecificRecordBase) sensorEvent.getPayload();
        Instant eventTime = sensorEvent.getTimestamp();
        SensorStateAvro newState = new SensorStateAvro(eventTime, payload);

        log.debug("hubSnapshot={}", hubSnapshot);

        // Новый снапшот
        if (hubSnapshot == null) {
            hubSnapshot = new SensorsSnapshotAvro();
            hubSnapshot.setHubId(hubId);
            hubSnapshot.setTimestamp(eventTime);

            Map<String, SensorStateAvro> sensorsState = new HashMap<>();
            hubSnapshot.setSensorsState(sensorsState);
            sensorsState.put(deviceId, newState);

            allSnapshots.put(hubId, hubSnapshot);

            return true;
        }

        SensorStateAvro oldState = hubSnapshot.getSensorsState().get(deviceId);

        // Состояние датчика, который пока отсутствует в снапшоте.
        if (oldState == null) {
            hubSnapshot.getSensorsState().put(deviceId, newState);
            checkAndSetSnapshotTime(hubSnapshot, eventTime);
            return true;
        }

        // Если событие неактуально.
        if (eventTime.isBefore(oldState.getTimestamp())) {
            return false;
        }

        // Обновить данные датчика.
        hubSnapshot.getSensorsState().put(deviceId, newState);

        checkAndSetSnapshotTime(hubSnapshot, eventTime);

        return !Objects.equals(payload, oldState.getData());
    }

}