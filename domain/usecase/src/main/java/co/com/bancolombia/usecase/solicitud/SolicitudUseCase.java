package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.solicitud.gateways.NotificationGateway;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public Flux<Solicitud> searchSolicitudes(Solicitud criteria) {
        return solicitudRepository.findByExample(criteria);
    }

    public Mono<Solicitud> updateEstadoSolicitud(Integer id, EstadoSolicitud nuevoEstado, Boolean isAuto) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Solicitud no encontrada con el id: " + id)))
                .flatMap(solicitud -> {
                    solicitud.setEstado(nuevoEstado);
                    return solicitudRepository.save(solicitud)
                            .flatMap(savedSolicitud ->
                                Boolean.TRUE.equals(isAuto)
                                        ? Mono.just(savedSolicitud)
                                        : sendUpdateNotifications(savedSolicitud).thenReturn(savedSolicitud)
                            );
                    }
                );
    }

    private Mono<Solicitud> enrichSolicitudWithValidacion(Solicitud solicitud) {
        Map<String, Object> data = new HashMap<>();

        return tipoPrestamoGateway.findById(solicitud.getIdTipoPrestamo())
                .map(tipoPrestamo -> {
                    solicitud.setValidacionAutomatica(tipoPrestamo.getValidacionAutomatica());
                    data.put("solicitud", solicitud);
                    return data;
                })
                .flatMap(enrichedData ->
                        notificationGateway.sendSQSCapacity(enrichedData).thenReturn(solicitud))
                .defaultIfEmpty(solicitud); // In case tipoPrestamo is not found, return the original solicitud
    }

    private Mono<Void> sendUpdateNotifications(Solicitud solicitud) {
        List<Mono<Void>> notificationTasks = new ArrayList<>();

        notificationTasks.add(notificationGateway.sendSQSStatusChange(solicitud));

        if (solicitud.getEstado() == EstadoSolicitud.RECHAZADO) {
            notificationTasks.add(notificationGateway.sendSQSStatusRechazado(solicitud.getId()));
        }

        return Mono.when(notificationTasks);
    }

}