package co.com.bancolombia.model.solicitud.gateways;

import co.com.bancolombia.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NotificationGateway {
    Mono<Void> sendSQSCapacity(Map<String, Object> message);
    Mono<Void> sendSQSStatusChange(Solicitud solicitud);
    Mono<Void> sendSQSStatusRechazado(Integer loanId);
}