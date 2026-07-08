package shm.telemetry.aggregator.serialization;

import avro.serialization.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public class SensorEventAvroDeserializer extends GeneralAvroDeserializer<SensorEventAvro> {

    public SensorEventAvroDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }

}