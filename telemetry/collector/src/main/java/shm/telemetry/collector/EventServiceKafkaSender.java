package shm.telemetry.collector;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import shm.telemetry.collector.mapper.AvroHubEventMapper;
import shm.telemetry.collector.mapper.AvroSensorEventMapper;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.sensor.BaseSensorEvent;

@Service
public class EventServiceKafkaSender implements EventService {
    private final Producer<Void, SpecificRecordBase> kafkaProducer;

    public EventServiceKafkaSender(Producer<Void, SpecificRecordBase> producer) {
        this.kafkaProducer = producer;
    }

    @Override
    public void sendSensorEvent(BaseSensorEvent sensorEvent) {
        SensorEventAvro sensorEventAvro = AvroSensorEventMapper.sensorEventToAvro(sensorEvent);

        String topic = "telemetry.sensors.v1";
        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(topic, sensorEventAvro);

        kafkaProducer.send(record);
    }

    @Override
    public void sendHubEvent(BaseHubEvent hubEvent) {
        HubEventAvro hubEventAvro = AvroHubEventMapper.hubEventToAvro(hubEvent);

        String topic = "telemetry.hubs.v1";
        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(topic, hubEventAvro);

        kafkaProducer.send(record);
    }
}
