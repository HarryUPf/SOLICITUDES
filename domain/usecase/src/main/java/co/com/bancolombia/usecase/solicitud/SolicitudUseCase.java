package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.solicitud.gateways.NotificationGateway;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoGateway tipoPrestamoGateway;
    private final NotificationGateway notificationGateway;

    public Mono<Solicitud> createSolicitud(Solicitud solicitud) {
        // Business logic can be added here. For example, setting a default state.
        if (solicitud.getEstado() == null) {
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        }
        return solicitudRepository.save(solicitud)
                .flatMap(this::enrichSolicitudWithValidacion);
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

//    public Mono<String> updateEstadoSolicitud(Integer id, EstadoSolicitud nuevoEstado) {
//        Map<String, Object> data = new HashMap<>();
//        data.put("subject", "subject here");
//        data.put("email_body", "email body here");

    public Mono<Solicitud> updateEstadoSolicitud(Integer id, EstadoSolicitud nuevoEstado) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Solicitud no encontrada con el id: " + id)))
                .flatMap(solicitud -> {
                    // Here you could add business logic to validate the state transition
                    solicitud.setEstado(nuevoEstado);
                    return solicitudRepository.save(solicitud)
                            // After saving, send notification and then return the saved object
//                            .flatMap(savedSolicitud -> notificationGateway.sendStatusUpdateNotification(savedSolicitud).thenReturn(savedSolicitud))
                            .flatMap(savedSolicitud -> notificationGateway.sendSQSTest(savedSolicitud).thenReturn(savedSolicitud))
//                            .flatMap(savedSolicitud -> notificationGateway.sendSQSTest(data).thenReturn("NIGGA"))
                    ;
                });
    }

    private Mono<Solicitud> enrichSolicitudWithValidacion(Solicitud solicitud) {
        Map<String, Object> data = new HashMap<>();

        return tipoPrestamoGateway.findById(solicitud.getIdTipoPrestamo())
                .map(tipoPrestamo -> {
                    solicitud.setValidacionAutomatica(tipoPrestamo.getValidacionAutomatica());
                    data.put("subject", "subject here");
                    data.put("email_body", "email body here");
                    data.put("solicitud", solicitud);
                    return data;
                })
                .flatMap(enrichedData ->
                        notificationGateway.sendSQSCapacity(enrichedData).thenReturn(solicitud))
                .defaultIfEmpty(solicitud); // In case tipoPrestamo is not found, return the original solicitud
    }

}