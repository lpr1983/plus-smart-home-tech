package shm.telemetry.analyzer.processor.snapshot_event;

import net.devh.boot.grpc.client.inject.GrpcClient;
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
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import shm.telemetry.analyzer.kafka.KafkaProperties;
import shm.telemetry.analyzer.model.Action;
import shm.telemetry.analyzer.model.Condition;
import shm.telemetry.analyzer.model.ConditionOperation;
import shm.telemetry.analyzer.model.ConditionType;
import shm.telemetry.analyzer.model.Scenario;
import shm.telemetry.analyzer.repository.ScenarioRepository;
import shm.telemetry.analyzer.serialization.SensorsSnapshotAvroDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class SnapshotProcessor {
    private final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);
    private final KafkaProperties kafkaProperties;
    private final ScenarioRepository scenarioRepository;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotProcessor(KafkaProperties kafkaProperties, ScenarioRepository scenarioRepository,
                             @GrpcClient("hub-router")
                             HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.kafkaProperties = kafkaProperties;
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    public void run() {
        List<String> consumerTopics = List.of(kafkaProperties.consumer().snapshotProcessor().topic());
        Duration pollTimeout = Duration.ofMillis(kafkaProperties.consumer().pollTimeout());

        try (KafkaConsumer<Void, SensorsSnapshotAvro> consumer = createConsumer()) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(consumerTopics);

            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);

                consumer.commitSync();

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
                    processRecord(record);
                    log.debug("polled SensorsSnapshotAvro {}", record.value());
                }

                //consumer.commitSync();
            }
        } catch (WakeupException ignore) {
            log.info("Завершение работы SnapshotProcessor");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки данных SnapshotProcessor", e);
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

    private void processRecord(ConsumerRecord<Void, SensorsSnapshotAvro> record) {
        SensorsSnapshotAvro snapshot = record.value();

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        for (Scenario s : scenarios) {
            boolean doAction = compareConditionsAndHubState(s, snapshot);

            if (doAction) {
                for (Map.Entry<String, Action> entry : s.getActions().entrySet()) {
                    Action action = entry.getValue();
                    String deviceId = entry.getKey();

                    ActionTypeProto actionTypeProto = ActionTypeProto.valueOf(action.getType().name());

                    DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                            .setSensorId(deviceId)
                            .setType(actionTypeProto)
                            .build();

                    Integer actionValue = action.getValue();
                    if (actionValue != null) {
                        deviceActionProto = DeviceActionProto.newBuilder(deviceActionProto)
                                .setValue(action.getValue()).build();
                    }


                    Instant now = Instant.now();
                    com.google.protobuf.Timestamp gts = com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build();

                    DeviceActionRequest request = DeviceActionRequest.newBuilder()
                            .setHubId(snapshot.getHubId())
                            .setScenarioName(s.getName())
                            .setAction(deviceActionProto)
                            .setTimestamp(gts)
                            .build();

                    try {
                        log.info("do action request={}", request);
                        hubRouterClient.withDeadlineAfter(3, TimeUnit.SECONDS).handleDeviceAction(request);
                    } catch (Exception e) {
                        log.error("Error during hubRouterClient.handleDeviceAction", e);
                    }

                }
            }
        }
    }

    private boolean compareConditionsAndHubState(Scenario s, SensorsSnapshotAvro snapshot) {
        Map<String, Condition> conditions = s.getConditions();
        if (conditions == null || conditions.size() == 0) {
            return false;
        }

        boolean methodResult = true;

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
            String deviceId = entry.getKey();
            Condition condition = entry.getValue();

            SensorStateAvro sensorState = sensorsState.get(deviceId);
            if (sensorState == null) {
                methodResult = false;
                break;
            }

            ConditionType conditionType = condition.getType(); // На что условие
            Integer intValue = condition.getIntValue(); // Значение условия: численное
            Boolean boolValue = condition.getBoolValue(); // Значение условия: булево
            ConditionOperation operation = condition.getOperation(); // Операция сравнения

            Integer intSensorValue = getIntSensorValue(sensorState, conditionType);
            Boolean boolSensorValue = getBoolSensorValue(sensorState, conditionType);

            Boolean compareResult = null;

            if (operation == ConditionOperation.EQUALS) {
                if (boolValue != null) {
                    compareResult = Objects.equals(boolValue, boolSensorValue);
                } else if (intValue != null) {
                    compareResult = Objects.equals(intValue, intSensorValue);
                }
            } else if (operation == ConditionOperation.GREATER_THAN) {
                if (intValue != null && intSensorValue != null) {
                    compareResult = intSensorValue > intValue;
                }
            } else if (operation == ConditionOperation.LOWER_THAN) {
                if (intValue != null && intSensorValue != null) {
                    compareResult = intSensorValue < intValue;
                }
            }

            if (compareResult == null || !compareResult) {
                methodResult = false;
                break;
            }

        }

        return methodResult;
    }

    private Integer getCO2Level(SpecificRecordBase sensorData) {
        Integer value = null;
        if (sensorData.getClass() == ClimateSensorAvro.class) {
            value = ((ClimateSensorAvro) sensorData).getCo2Level();
        }
        return value;
    }

    private Integer getHumidity(SpecificRecordBase sensorData) {
        Integer value = null;
        if (sensorData.getClass() == ClimateSensorAvro.class) {
            value = ((ClimateSensorAvro) sensorData).getHumidity();
        }
        return value;
    }

    private Integer getTemperature(SpecificRecordBase sensorData) {
        Integer value = null;
        if (sensorData.getClass() == ClimateSensorAvro.class) {
            value = ((ClimateSensorAvro) sensorData).getTemperatureC();
        } else if (sensorData.getClass() == TemperatureSensorAvro.class) {
            value = ((TemperatureSensorAvro) sensorData).getTemperatureC();
        }
        return value;
    }

    private Integer getLuminosity(SpecificRecordBase sensorData) {
        Integer value = null;
        if (sensorData.getClass() == LightSensorAvro.class) {
            value = ((LightSensorAvro) sensorData).getLuminosity();
        }
        return value;
    }

    private Integer getIntSensorValue(SensorStateAvro sensorState, ConditionType conditionType) {
        List<ConditionType> suitableConditions = List.of(ConditionType.CO2LEVEL,
                ConditionType.HUMIDITY,
                ConditionType.LUMINOSITY,
                ConditionType.TEMPERATURE
        );

        if (!suitableConditions.contains(conditionType)) {
            return null;
        }

        Integer value = switch (conditionType) {
            case CO2LEVEL -> getCO2Level((SpecificRecordBase) sensorState.getData());
            case HUMIDITY -> getHumidity((SpecificRecordBase) sensorState.getData());
            case TEMPERATURE -> getTemperature((SpecificRecordBase) sensorState.getData());
            case LUMINOSITY -> getLuminosity((SpecificRecordBase) sensorState.getData());
            default -> null;
        };

        return value;
    }

    private Boolean getMotion(SpecificRecordBase sensorData) {
        Boolean value = null;
        if (sensorData.getClass() == MotionSensorAvro.class) {
            value = ((MotionSensorAvro) sensorData).getMotion();
        }
        return value;
    }

    private Boolean getSwitch(SpecificRecordBase sensorData) {
        Boolean value = null;
        if (sensorData.getClass() == SwitchSensorAvro.class) {
            value = ((SwitchSensorAvro) sensorData).getState();
        }
        return value;
    }

    private Boolean getBoolSensorValue(SensorStateAvro sensorState, ConditionType conditionType) {
        List<ConditionType> suitableConditions = List.of(ConditionType.CO2LEVEL,
                ConditionType.MOTION,
                ConditionType.SWITCH
        );

        if (!suitableConditions.contains(conditionType)) {
            return null;
        }

        Boolean value = switch (conditionType) {
            case MOTION -> getMotion((SpecificRecordBase) sensorState.getData());
            case SWITCH -> getSwitch((SpecificRecordBase) sensorState.getData());
            default -> null;
        };

        return value;
    }
}
