package shm.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

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
