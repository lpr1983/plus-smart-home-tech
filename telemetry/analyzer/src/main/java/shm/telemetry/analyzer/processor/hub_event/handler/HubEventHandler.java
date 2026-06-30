package shm.telemetry.analyzer.processor.hub_event.handler;

public interface HubEventHandler<T> {

    void handle(String hubId, T payload);

}
