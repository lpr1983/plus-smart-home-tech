package shm.telemetry.analyzer.processor.hub_event.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import shm.telemetry.analyzer.model.Action;
import shm.telemetry.analyzer.model.ActionType;
import shm.telemetry.analyzer.model.Condition;
import shm.telemetry.analyzer.model.ConditionOperation;
import shm.telemetry.analyzer.model.ConditionType;
import shm.telemetry.analyzer.model.Scenario;
import shm.telemetry.analyzer.repository.ActionRepository;
import shm.telemetry.analyzer.repository.ConditionRepository;
import shm.telemetry.analyzer.repository.ScenarioRepository;

import java.util.Optional;

@Component
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {
    private final Logger log = LoggerFactory.getLogger(ScenarioAddedEventHandler.class);
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;

    public ScenarioAddedEventHandler(ActionRepository actionRepository,
                                     ConditionRepository conditionRepository,
                                     ScenarioRepository scenarioRepository) {
        this.actionRepository = actionRepository;
        this.conditionRepository = conditionRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    @Override
    public void handle(String hubId, ScenarioAddedEventAvro payload) {
        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, payload.getName());
        if (searchResult.isPresent()) {
            log.info("Scenario for hubId={} and name={} is already exists", hubId, payload.getName());
            return;
        }

        Scenario scenario = new Scenario();
        scenario.setName(payload.getName());
        scenario.setHubId(hubId);

        for (DeviceActionAvro a : payload.getActions()) {
            Action action = new Action();
            action.setValue(a.getValue());
            ActionType type = ActionType.valueOf(a.getType().name());
            action.setType(type);
            actionRepository.save(action);
            log.info("created action={}", action);
            scenario.getActions().put(a.getSensorId(), action);
        }

        for (ScenarioConditionAvro sc : payload.getConditions()) {
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
}
