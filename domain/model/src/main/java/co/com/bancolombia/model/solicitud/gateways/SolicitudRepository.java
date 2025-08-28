package co.com.bancolombia.model.solicitud.gateways;

import co.com.bancolombia.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {
    Flux<Solicitud> findAll();
    Mono<Solicitud> findById(Integer id);
    Mono<Solicitud> save(Solicitud solicitud);
    Flux<Solicitud> findByExample(Solicitud solicitud);
}