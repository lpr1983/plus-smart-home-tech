package shm.telemetry.analyzer.serialization;

import avro.serialization.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class HubEventAvroDeserializer extends GeneralAvroDeserializer<HubEventAvro> {

    public HubEventAvroDeserializer() {
        super(HubEventAvro.getClassSchema());
    }

}
