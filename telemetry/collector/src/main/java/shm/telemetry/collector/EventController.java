package shm.telemetry.collector;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shm.telemetry.collector.model.event.hub.BaseHubEvent;
import shm.telemetry.collector.model.event.sensor.BaseSensorEvent;

@RestController
@RequestMapping("/events")
public class EventController {

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.OK)
    public void postSensorEvent(@RequestBody BaseSensorEvent sensorEvent) {
        String weAreHere = "yes";
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.OK)
    public void postHubEvent(@RequestBody BaseHubEvent hubEvent) {
        String weAreHere = "yes";
    }
}
