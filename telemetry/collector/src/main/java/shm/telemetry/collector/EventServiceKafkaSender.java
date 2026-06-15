package shm.telemetry.collector;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
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
    private final String sensorEventsTopic;
    private final String hubEventsTopic;

    public EventServiceKafkaSender(Producer<Void, SpecificRecordBase> producer,
                                   @Value("${kafka.topic.sensor-events}") String sensorEventsTopic,
                                   @Value("${kafka.topic.hub-events}") String hubEventsTopic) {
        this.kafkaProducer = producer;
        this.sensorEventsTopic = sensorEventsTopic;
        this.hubEventsTopic = hubEventsTopic;
    }

    @Override
    public void sendSensorEvent(BaseSensorEvent sensorEvent) {
        SensorEventAvro sensorEventAvro = AvroSensorEventMapper.sensorEventToAvro(sensorEvent);

        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(sensorEventsTopic, sensorEventAvro);

        kafkaProducer.send(record);
    }

    @Override
    public void sendHubEvent(BaseHubEvent hubEvent) {
        HubEventAvro hubEventAvro = AvroHubEventMapper.hubEventToAvro(hubEvent);

        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(hubEventsTopic, hubEventAvro);

        kafkaProducer.send(record);
    }
}
