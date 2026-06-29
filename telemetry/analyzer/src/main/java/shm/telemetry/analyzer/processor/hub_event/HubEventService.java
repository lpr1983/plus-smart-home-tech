package shm.telemetry.analyzer.processor.hub_event;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

public interface HubEventService {

    void handleDeviceAddedEvent(String hubId, DeviceAddedEventAvro payload);

    void handleDeviceRemovedEvent(String hubId, DeviceRemovedEventAvro payload);

    void handleScenarioAddedEvent(String hubId, ScenarioAddedEventAvro scenarioAddedEventAvro);

    void handleScenarioRemovedEvent(String hubId, ScenarioRemovedEventAvro payload);

}
