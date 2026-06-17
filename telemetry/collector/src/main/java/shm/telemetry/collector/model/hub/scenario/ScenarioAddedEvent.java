package shm.telemetry.collector.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.hub.HubEventType;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ScenarioCondition> getConditions() {
        return conditions;
    }

    public void setConditions(Set<ScenarioCondition> conditions) {
        this.conditions = conditions;
    }

    public Set<DeviceAction> getActions() {
        return actions;
    }

    public void setActions(Set<DeviceAction> actions) {
        this.actions = actions;
    }
}
