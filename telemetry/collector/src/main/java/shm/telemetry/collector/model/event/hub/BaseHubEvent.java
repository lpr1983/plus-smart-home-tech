package shm.telemetry.collector.model.event.hub;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import shm.telemetry.collector.model.event.hub.device.DeviceAddedEvent;
import shm.telemetry.collector.model.event.hub.device.DeviceRemovedEvent;
import shm.telemetry.collector.model.event.hub.scenario.ScenarioAddedEvent;
import shm.telemetry.collector.model.event.hub.scenario.ScenarioRemovedEvent;
import shm.telemetry.collector.model.event.sensor.ClimateSensorEvent;
import shm.telemetry.collector.model.event.sensor.LightSensorEvent;
import shm.telemetry.collector.model.event.sensor.MotionSensorEvent;
import shm.telemetry.collector.model.event.sensor.SensorEventType;
import shm.telemetry.collector.model.event.sensor.SwitchSensorEvent;
import shm.telemetry.collector.model.event.sensor.TemperatureSensorEvent;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        defaultImpl = BaseHubEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedEvent.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedEvent.class, name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedEvent.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemovedEvent.class, name = "SCENARIO_REMOVED")
})
public abstract class BaseHubEvent {

    private String description;

    @NotBlank
    private String hubId;

    private Instant timestamp = Instant.now();

    public abstract HubEventType getType();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
