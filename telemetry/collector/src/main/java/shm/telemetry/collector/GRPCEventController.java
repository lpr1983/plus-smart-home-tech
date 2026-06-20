package shm.telemetry.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import shm.telemetry.collector.mapper.ProtoHubEventMapper;
import shm.telemetry.collector.mapper.ProtoSensorEventMapper;
import shm.telemetry.collector.model.hub.BaseHubEvent;
import shm.telemetry.collector.model.sensor.BaseSensorEvent;

@GrpcService
public class GRPCEventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final EventService eventService;

    public GRPCEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            BaseSensorEvent event =
                    ProtoSensorEventMapper.toModel(request);

            eventService.sendSensorEvent(event);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            BaseHubEvent event =
                    ProtoHubEventMapper.toModel(request);

            eventService.sendHubEvent(event);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}