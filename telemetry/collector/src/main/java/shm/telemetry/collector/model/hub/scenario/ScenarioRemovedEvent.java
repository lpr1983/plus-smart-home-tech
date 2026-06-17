package shm.telemetry.collector.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.hub.HubEventType;

public class ScenarioRemovedEvent extends BaseHubEvent {
    @NotBlank
    @Size(min = 3)
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
