package shm.telemetry.analyzer.processor;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import shm.telemetry.analyzer.SensorRepository;
import shm.telemetry.analyzer.kafka.KafkaProperties;
import shm.telemetry.analyzer.model.Sensor;
import shm.telemetry.analyzer.serialization.HubEventAvroDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class HubEventProcessor implements Runnable {
    private final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);
    private final KafkaProperties kafkaProperties;
    private final SensorRepository sensorRepository;

    public HubEventProcessor(KafkaProperties kafkaProperties,
                             SensorRepository sensorRepository) {
        this.kafkaProperties = kafkaProperties;
        this.sensorRepository = sensorRepository;
    }

    @Override
    public void run() {
        List<String> consumerTopics = List.of(kafkaProperties.consumer().hubEventProcessor().topic());
        Duration pollTimeout = Duration.ofMillis(kafkaProperties.consumer().pollTimeout());

        try (KafkaConsumer<Void, HubEventAvro> consumer = createConsumer()) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(consumerTopics);

            while (true) {
                ConsumerRecords<Void, HubEventAvro> records = consumer.poll(pollTimeout);

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<Void, HubEventAvro> record : records) {
                    HubEventAvro event = record.value();

                    String hubId = event.getHubId();

                    SpecificRecordBase payload = (SpecificRecordBase) event.getPayload();
                    if (payload.getClass() == DeviceAddedEventAvro.class) {

                        String sensorId = ((DeviceAddedEventAvro) payload).getId();
                        Sensor sensor = new Sensor();
                        sensor.setId(sensorId);
                        sensor.setHubId(hubId);
                        sensorRepository.save(sensor);

                        log.info("created sensor id={}, hubId={}", sensorId, hubId);

                    } else if (payload.getClass() == DeviceRemovedEventAvro.class) {

                        String sensorId = ((DeviceRemovedEventAvro) payload).getId();
                        sensorRepository.deleteByIdAndHubId(sensorId, hubId);

                        log.info("delete sensor id={}, hubId={}", sensorId, hubId);
                    } else if (payload.getClass() == ScenarioAddedEventAvro.class) {
                        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) payload;

                    }

                    log.debug("polled HubEventAvro value={}, payload type={}", event, event.getPayload().getClass().getName());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignore) {
            log.info("Завершение работы HubEventProcessor");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки данных HubEventProcessor", e);
            throw new RuntimeException("Critical error in HubEventProcessor", e);
        }

    }

    private KafkaConsumer<Void, HubEventAvro> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.consumer().hubEventProcessor().clientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.consumer().hubEventProcessor().groupId());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.server());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventAvroDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Отключение автокомита
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // При вхождении в новую группу забирать с начала

        return new KafkaConsumer<>(properties);
    }

}
