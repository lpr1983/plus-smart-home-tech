package shm.telemetry.collector.model.event.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import shm.telemetry.collector.model.event.hub.BaseHubEvent;
import shm.telemetry.collector.model.event.hub.HubEventType;

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
