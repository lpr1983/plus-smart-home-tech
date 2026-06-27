package shm.telemetry.analyzer.serialization;

import avro.serialization.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SensorsSnapshotAvroDeserializer extends GeneralAvroDeserializer<SensorsSnapshotAvro> {

    public SensorsSnapshotAvroDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }

}
