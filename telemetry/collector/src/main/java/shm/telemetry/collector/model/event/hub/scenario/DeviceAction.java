package shm.telemetry.collector.model.event.hub.scenario;

public class DeviceAction {
    private String description;
    private String sensorId;
    private DeviceActionType type;
    private Integer value;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public DeviceActionType getType() {
        return type;
    }

    public void setType(DeviceActionType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
