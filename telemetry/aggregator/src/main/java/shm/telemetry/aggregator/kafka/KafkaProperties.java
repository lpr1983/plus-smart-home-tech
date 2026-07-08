package shm.telemetry.aggregator.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka")
@Validated
public record KafkaProperties(
        @NotBlank
        String server,
        Long retryPeriodMs,
        @Valid
        Producer producer,
        @Valid
        Consumer consumer
) {
        public record Producer(
                @NotBlank
                String topic
        ) {
        }

        public record Consumer(
                @NotBlank
                String topic,
                @NotBlank
                String clientId,
                @NotBlank
                String groupId,
                @NotNull
                Long consumeAttemptTimeoutMs
        ) {
        }
}



