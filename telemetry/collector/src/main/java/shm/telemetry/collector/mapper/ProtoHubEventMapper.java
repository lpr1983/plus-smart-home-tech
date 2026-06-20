package shm.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.hub.device.DeviceAddedEvent;
import shm.telemetry.collector.model.hub.device.DeviceRemovedEvent;
import shm.telemetry.collector.model.hub.device.DeviceType;
import shm.telemetry.collector.model.hub.scenario.ActionType;
import shm.telemetry.collector.model.hub.scenario.ConditionOperation;
import shm.telemetry.collector.model.hub.scenario.ConditionType;
import shm.telemetry.collector.model.hub.scenario.DeviceAction;
import shm.telemetry.collector.model.hub.scenario.ScenarioAddedEvent;
import shm.telemetry.collector.model.hub.scenario.ScenarioCondition;
import shm.telemetry.collector.model.hub.scenario.ScenarioRemovedEvent;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ProtoHubEventMapper {

    private ProtoHubEventMapper() {
    }

    public static BaseHubEvent toModel(HubEventProto proto) {
        if (proto == null) {
            throw new IllegalArgumentException("Hub event proto can't be null");
        }

        BaseHubEvent event;

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED:
                DeviceAddedEvent deviceAddedEvent = new DeviceAddedEvent();
                deviceAddedEvent.setId(proto.getDeviceAdded().getId());
                deviceAddedEvent.setDeviceType(toDeviceType(proto.getDeviceAdded().getType()));
                event = deviceAddedEvent;
                break;

            case DEVICE_REMOVED:
                DeviceRemovedEvent deviceRemovedEvent = new DeviceRemovedEvent();
                deviceRemovedEvent.setId(proto.getDeviceRemoved().getId());
                event = deviceRemovedEvent;
                break;

            case SCENARIO_ADDED:
                ScenarioAddedEvent scenarioAddedEvent = new ScenarioAddedEvent();
                scenarioAddedEvent.setName(proto.getScenarioAdded().getName());

                Set<ScenarioCondition> conditions = new LinkedHashSet<>();
                for (ScenarioConditionProto condition : proto.getScenarioAdded().getConditionList()) {
                    conditions.add(toScenarioCondition(condition));
                }
                scenarioAddedEvent.setConditions(conditions);

                Set<DeviceAction> actions = new LinkedHashSet<>();
                for (DeviceActionProto action : proto.getScenarioAdded().getActionList()) {
                    actions.add(toDeviceAction(action));
                }
                scenarioAddedEvent.setActions(actions);

                event = scenarioAddedEvent;
                break;

            case SCENARIO_REMOVED:
                ScenarioRemovedEvent scenarioRemovedEvent = new ScenarioRemovedEvent();
                scenarioRemovedEvent.setName(proto.getScenarioRemoved().getName());
                event = scenarioRemovedEvent;
                break;

            case PAYLOAD_NOT_SET:
            default:
                throw new IllegalArgumentException("Hub event payload is not set");
        }

        event.setHubId(proto.getHubId());
        if (proto.hasTimestamp()) {
            event.setTimestamp(toInstant(proto.getTimestamp()));
        }

        return event;
    }

    private static ScenarioCondition toScenarioCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(toConditionType(proto.getType()));
        condition.setOperation(toConditionOperation(proto.getOperation()));

        switch (proto.getValueCase()) {
            case BOOL_VALUE:
                condition.setBoolValue(proto.getBoolValue());
                break;
            case INT_VALUE:
                condition.setValue(proto.getIntValue());
                break;
            case VALUE_NOT_SET:
            default:
                condition.setValue(null);
                break;
        }

        return condition;
    }

    private static DeviceAction toDeviceAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(toActionType(proto.getType()));
        action.setValue(proto.hasValue() ? proto.getValue() : null);
        return action;
    }

    private static DeviceType toDeviceType(DeviceTypeProto proto) {
        if (proto == DeviceTypeProto.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unrecognized device type");
        }
        return DeviceType.valueOf(proto.name());
    }

    private static ConditionType toConditionType(ConditionTypeProto proto) {
        if (proto == ConditionTypeProto.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unrecognized condition type");
        }
        return ConditionType.valueOf(proto.name());
    }

    private static ConditionOperation toConditionOperation(ConditionOperationProto proto) {
        if (proto == ConditionOperationProto.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unrecognized condition operation");
        }
        return ConditionOperation.valueOf(proto.name());
    }

    private static ActionType toActionType(ActionTypeProto proto) {
        if (proto == ActionTypeProto.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unrecognized action type");
        }
        return ActionType.valueOf(proto.name());
    }

    private static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
