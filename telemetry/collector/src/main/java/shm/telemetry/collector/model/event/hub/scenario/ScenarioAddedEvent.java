package shm.telemetry.collector.model.event.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import shm.telemetry.collector.model.event.hub.BaseHubEvent;
import shm.telemetry.collector.model.event.hub.HubEventType;

import java.util.Set;

public class ScenarioAddedEvent extends BaseHubEvent {
    @NotBlank
    @Size(min = 3)
    private String name;

    @NotEmpty
    Set<ScenarioCondition> conditions;

    @NotEmpty
    Set<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

}
