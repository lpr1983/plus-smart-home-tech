package shm.telemetry.analyzer.processor.hub_event.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import shm.telemetry.analyzer.model.Action;
import shm.telemetry.analyzer.model.Condition;
import shm.telemetry.analyzer.model.Scenario;
import shm.telemetry.analyzer.repository.ActionRepository;
import shm.telemetry.analyzer.repository.ConditionRepository;
import shm.telemetry.analyzer.repository.ScenarioRepository;

import java.util.Optional;

@Component
public class ScenarioRemovedEventHandler implements HubEventHandler<ScenarioRemovedEventAvro> {
    private final Logger log = LoggerFactory.getLogger(ScenarioRemovedEventHandler.class);
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;

    public ScenarioRemovedEventHandler(ActionRepository actionRepository,
                                       ConditionRepository conditionRepository,
                                       ScenarioRepository scenarioRepository) {
        this.actionRepository = actionRepository;
        this.conditionRepository = conditionRepository;
        this.scenarioRepository = scenarioRepository;
    }


    @Transactional
    @Override
    public void handle(String hubId, ScenarioRemovedEventAvro payload) {
        String name = payload.getName();
        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, name);

        if (searchResult.isEmpty()) {
            return;
        }
        // Все остальное удалится само т.к. CascadeType.REMOVE у Scenario.
        scenarioRepository.deleteByHubIdAndName(hubId, ((ScenarioRemovedEventAvro) payload).getName());
        log.info("scenario removed name={}, hubId={}", name, hubId);
    }
}
