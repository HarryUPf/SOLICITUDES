package co.com.bancolombia.model.solicitud.gateways;

import co.com.bancolombia.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NotificationGateway {
//    Mono<Void> sendStatusUpdateNotification(Solicitud solicitud);
    Mono<Void> sendSQSCapacity(Map<String, Object> message);
    Mono<Void> sendSQSTest(Solicitud solicitud);
}