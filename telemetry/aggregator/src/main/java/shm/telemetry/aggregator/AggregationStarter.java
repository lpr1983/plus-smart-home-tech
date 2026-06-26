package shm.telemetry.aggregator;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

@Component
public class AggregationStarter {
    private final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final KafkaConsumer<Void, SensorEventAvro> consumer;
    private final String sensorEventsTopic;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final Duration consumeAttemptTimeout;

    private final Producer<Void, SpecificRecordBase> producer;
    private final String snapshotsTopic;

    private final Map<String, SensorsSnapshotAvro> allSnapshots;

    public AggregationStarter(KafkaConsumer<Void, SensorEventAvro> consumer,
                              @Value("${kafka.consumer.topic}") String sensorEventsTopic,
                              @Value("${kafka.consumer.consume-attempt-timeout-ms}") int consumeAttemptTimeout,
                              Producer<Void, SpecificRecordBase> producer,
                              @Value("${kafka.producer.topic}") String snapshotsTopic
    ) {
        this.consumer = consumer;
        this.sensorEventsTopic = sensorEventsTopic;
        this.consumeAttemptTimeout = Duration.ofMillis(consumeAttemptTimeout);
        this.allSnapshots = new HashMap<>();
        this.producer = producer;
        this.snapshotsTopic = snapshotsTopic;
    }

    public void start() {

        try {
            consumer.subscribe(List.of(sensorEventsTopic));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                Set<String> hubsIdsToSendSnapshots = new HashSet<>();

                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(consumeAttemptTimeout);
                for (ConsumerRecord<Void, SensorEventAvro> record : records) {

                    log.debug("Polled object={}", record.value());

                    SensorEventAvro sensorEventAvro = record.value();

                    boolean updated = updateState(sensorEventAvro);
                    if (updated) {
                        hubsIdsToSendSnapshots.add(sensorEventAvro.getHubId());
                    }
                }

                List<Future<RecordMetadata>> sendResults = new ArrayList<>();

                for (String hubId : hubsIdsToSendSnapshots) {
                    SensorsSnapshotAvro snapshotAvro = allSnapshots.get(hubId);
                    ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(snapshotsTopic, snapshotAvro);

                    Future<RecordMetadata> future = producer.send(record);
                    sendResults.add(future);
                }

                for (Future<RecordMetadata> result : sendResults) {
                    result.get();
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }

            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();

                log.info("Закрываем продюсер");
                producer.close();
            }
        }
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