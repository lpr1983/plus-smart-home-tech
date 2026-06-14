package shm.telemetry.collector.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.hub.HubEventType;

public class DeviceAddedEvent extends BaseHubEvent {

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    @NotBlank
    private String id;

    @NotNull
    private DeviceType deviceType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
