package shm.telemetry.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shm.telemetry.collector.exception.ValidationException;
import shm.telemetry.collector.model.event.hub.BaseHubEvent;
import shm.telemetry.collector.model.event.hub.HubEventType;
import shm.telemetry.collector.model.event.hub.device.DeviceAddedEvent;
import shm.telemetry.collector.model.event.hub.device.DeviceRemovedEvent;
import shm.telemetry.collector.model.event.hub.scenario.ScenarioAddedEvent;
import shm.telemetry.collector.model.event.hub.scenario.ScenarioRemovedEvent;
import shm.telemetry.collector.model.event.sensor.BaseSensorEvent;
import shm.telemetry.collector.model.event.sensor.ClimateSensorEvent;
import shm.telemetry.collector.model.event.sensor.LightSensorEvent;
import shm.telemetry.collector.model.event.sensor.MotionSensorEvent;
import shm.telemetry.collector.model.event.sensor.SensorEventType;
import shm.telemetry.collector.model.event.sensor.SwitchSensorEvent;
import shm.telemetry.collector.model.event.sensor.TemperatureSensorEvent;

import java.util.Set;

@RestController
@RequestMapping("/events")
public class EventController {
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public EventController(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.OK)
    public void postSensorEvent(@RequestBody String jsonString) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(jsonString);

        JsonNode typeNode = getTypeNodeOrThrow(node);

        SensorEventType type = SensorEventType.parse(typeNode.asText());
        BaseSensorEvent sensorEvent = switch (type) {
            case CLIMATE_SENSOR_EVENT -> objectMapper.treeToValue(node, ClimateSensorEvent.class);
            case LIGHT_SENSOR_EVENT -> objectMapper.treeToValue(node, LightSensorEvent.class);
            case MOTION_SENSOR_EVENT -> objectMapper.treeToValue(node, MotionSensorEvent.class);
            case SWITCH_SENSOR_EVENT -> objectMapper.treeToValue(node, SwitchSensorEvent.class);
            case TEMPERATURE_SENSOR_EVENT -> objectMapper.treeToValue(node, TemperatureSensorEvent.class);
        };

        validate(sensorEvent);

        // Отправить в кафку
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.OK)
    public void postHubEvent(@RequestBody String jsonString) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(jsonString);

        JsonNode typeNode = getTypeNodeOrThrow(node);

        HubEventType type = HubEventType.parse(typeNode.asText());
        BaseHubEvent hubEvent = switch (type) {
            case DEVICE_ADDED -> objectMapper.treeToValue(node, DeviceAddedEvent.class);
            case DEVICE_REMOVED -> objectMapper.treeToValue(node, DeviceRemovedEvent.class);
            case SCENARIO_ADDED -> objectMapper.treeToValue(node, ScenarioAddedEvent.class);
            case SCENARIO_REMOVED -> objectMapper.treeToValue(node, ScenarioRemovedEvent.class);
        };

        validate(hubEvent);

        // Отправить в кафку
    }

    private JsonNode getTypeNodeOrThrow(JsonNode node) {
        JsonNode typeNode = node.get("type");
        if (typeNode == null) {
            throw new ValidationException("Field \"type\" must be filled");
        }

        return typeNode;
    }

    private <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
