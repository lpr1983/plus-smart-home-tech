package shm.telemetry.collector.model.event.hub.scenario;

public class ScenarioCondition {
    private String description;
    private String sensorId;
    private ScenarioConditionType type;
    private ScenarioOperation operation;
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

    public ScenarioConditionType getType() {
        return type;
    }

    public void setType(ScenarioConditionType type) {
        this.type = type;
    }

    public ScenarioOperation getOperation() {
        return operation;
    }

    public void setOperation(ScenarioOperation operation) {
        this.operation = operation;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
