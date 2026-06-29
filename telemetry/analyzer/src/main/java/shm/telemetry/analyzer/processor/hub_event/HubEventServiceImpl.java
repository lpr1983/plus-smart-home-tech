package shm.telemetry.analyzer.processor.hub_event;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import shm.telemetry.analyzer.kafka.KafkaProperties;
import shm.telemetry.analyzer.model.Action;
import shm.telemetry.analyzer.model.ActionType;
import shm.telemetry.analyzer.model.Condition;
import shm.telemetry.analyzer.model.ConditionOperation;
import shm.telemetry.analyzer.model.ConditionType;
import shm.telemetry.analyzer.model.Scenario;
import shm.telemetry.analyzer.model.Sensor;
import shm.telemetry.analyzer.repository.ActionRepository;
import shm.telemetry.analyzer.repository.ConditionRepository;
import shm.telemetry.analyzer.repository.ScenarioRepository;
import shm.telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Component
public class HubEventServiceImpl implements HubEventService {
    private final Logger log = LoggerFactory.getLogger(HubEventServiceImpl.class);
    private final SensorRepository sensorRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;

    public HubEventServiceImpl(KafkaProperties kafkaProperties,
                               SensorRepository sensorRepository,
                               ActionRepository actionRepository,
                               ConditionRepository conditionRepository,
                               ScenarioRepository scenarioRepository) {
        this.sensorRepository = sensorRepository;
        this.actionRepository = actionRepository;
        this.conditionRepository = conditionRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    @Transactional
    public void handleDeviceAddedEvent(String hubId, DeviceAddedEventAvro payload) {
        String sensorId = ((DeviceAddedEventAvro) payload).getId();
        Optional<Sensor> searchResult = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (searchResult.isPresent()) {
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);

        log.info("created sensor id={}, hubId={}", sensorId, hubId);
    }

    @Override
    @Transactional
    public void handleDeviceRemovedEvent(String hubId, DeviceRemovedEventAvro payload) {
        String sensorId = payload.getId();

        sensorRepository.deleteByIdAndHubId(sensorId, hubId);
        log.info("delete sensor id={}, hubId={}", sensorId, hubId);
    }

    @Override
    @Transactional
    public void handleScenarioAddedEvent(String hubId, ScenarioAddedEventAvro scenarioAddedEventAvro) {

        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, scenarioAddedEventAvro.getName());
        if (searchResult.isPresent()) {
            return;
        }

        Scenario scenario = new Scenario();
        scenario.setName(scenarioAddedEventAvro.getName());
        scenario.setHubId(hubId);

        for (DeviceActionAvro a : scenarioAddedEventAvro.getActions()) {

            Action action = new Action();
            action.setValue(a.getValue());
            ActionType type = ActionType.valueOf(a.getType().name());
            action.setType(type);
            actionRepository.save(action);

            log.info("created action={}", action);

            scenario.getActions().put(a.getSensorId(), action);
        }

        for (ScenarioConditionAvro sc : scenarioAddedEventAvro.getConditions()) {

            Condition condition = new Condition();
            condition.setOperation(ConditionOperation.valueOf(sc.getOperation().name()));
            condition.setType(ConditionType.valueOf(sc.getType().name()));
            conditionRepository.save(condition);

            log.info("created condition={}", condition);

            scenario.getConditions().put(sc.getSensorId(), condition);
        }

        scenarioRepository.save(scenario);
        log.info("created scenario={}", scenario);
    }

    @Override
    @Transactional
    public void handleScenarioRemovedEvent(String hubId, ScenarioRemovedEventAvro payload) {
        String name = payload.getName();
        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, name);

        if (searchResult.isEmpty()) {
            return;
        }

        Scenario scenario = searchResult.get();
        for (Action a : scenario.getActions().values()) {
            actionRepository.delete(a);
        }
        for (Condition c : scenario.getConditions().values()) {
            conditionRepository.delete(c);
        }
        scenarioRepository.deleteByHubIdAndName(hubId, ((ScenarioRemovedEventAvro) payload).getName());
        log.info("scenario removed name={}, hubId={}", name, hubId);
    }

}
