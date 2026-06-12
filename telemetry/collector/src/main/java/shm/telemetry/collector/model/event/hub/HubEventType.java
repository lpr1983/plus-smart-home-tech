package shm.telemetry.collector.model.event.hub;

import shm.telemetry.collector.exception.ValidationException;

public enum HubEventType {
    DEVICE_ADDED,
    DEVICE_REMOVED,
    SCENARIO_ADDED,
    SCENARIO_REMOVED;

    public static HubEventType parse(String str) {
        try {
            return HubEventType.valueOf(str);
        } catch (Exception e) {
            throw new ValidationException(String.format("Неизвестный тип события: %s", str));
        }
    }
}
