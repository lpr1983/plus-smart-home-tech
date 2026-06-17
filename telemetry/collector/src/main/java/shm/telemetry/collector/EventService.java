package shm.telemetry.collector;

import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.sensor.BaseSensorEvent;

public interface EventService {
    void sendSensorEvent(BaseSensorEvent sensorEvent);

    void sendHubEvent(BaseHubEvent hubEvent);
}
