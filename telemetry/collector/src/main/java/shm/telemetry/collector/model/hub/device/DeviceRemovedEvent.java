package shm.telemetry.collector.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.hub.HubEventType;

public class DeviceRemovedEvent extends BaseHubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @NotBlank
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
