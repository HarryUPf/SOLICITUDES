package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;

    public Mono<Solicitud> createSolicitud(Solicitud solicitud) {
        // Business logic can be added here. For example, setting a default state.
        if (solicitud.getEstado() == null) {
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        }
        return solicitudRepository.save(solicitud);
    }

    public Flux<Solicitud> getAllSolicitudes() {
        return solicitudRepository.findAll();
    }

    public Mono<Solicitud> getSolicitudById(Integer id) {
        return solicitudRepository.findById(id);
    }

    public Flux<Solicitud> searchSolicitudes(Solicitud criteria) {
        return solicitudRepository.findByExample(criteria);
    }
}