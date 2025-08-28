package co.com.bancolombia.api;

import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class Handler {
    private static final Logger log = LoggerFactory.getLogger(Handler.class);
    private final SolicitudUseCase solicitudUseCase;
    // The ValidationHelper has been removed as it was specific to the User model.
    // Business validation should be handled within the SolicitudUseCase.
    public Mono<ServerResponse> createSolicitud(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Solicitud.class) // Recomendación: Usar un DTO para el request
                .doOnSubscribe(s -> log.info(">>> Starting createSolicitud flow"))
                .doOnNext(solicitud -> log.info("--- Creating solicitud: {}", solicitud))
                .flatMap(solicitudUseCase::createSolicitud)
                .flatMap(solicitudGuardada -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudGuardada))
                .doOnSuccess(response -> log.info("<<< createSolicitud flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error creando la solicitud", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Ocurrió un error interno al crear la solicitud."));
                });
    }

    public Mono<ServerResponse> findSolicitudes(ServerRequest request) {
        return Mono.just(request.queryParam("id"))
                .doOnSubscribe(s -> log.info(">>> Starting findSolicitudes flow"))
                .flatMap(optionalId -> {
                    if (optionalId.isPresent()) {
                        // Find by ID
                        String id = optionalId.get();
                        log.info("--- Finding solicitud by id: {}", id);
                        return solicitudUseCase.getSolicitudById(Integer.valueOf(id))
                                .flatMap(solicitud -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(solicitud))
                                .switchIfEmpty(ServerResponse.notFound().build());
                    } else {
                        // Find all
                        log.info("--- Finding all solicitudes");
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(solicitudUseCase.getAllSolicitudes(), Solicitud.class);
                    }
                })
                .doOnSuccess(response -> log.info("<<< findSolicitudes flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(NumberFormatException.class, e -> {
                    log.warn("!!! Invalid ID format provided in findSolicitudes: {}", e.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "El ID proporcionado no es un número válido."));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Internal error in findSolicitudes", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Ocurrió un error interno al procesar la solicitud."));
                });
    }

    public Mono<ServerResponse> searchSolicitudes(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Solicitud.class) // Recomendación: Usar un DTO para el request
                .doOnSubscribe(s -> log.info(">>> Starting searchSolicitudes flow"))
                .doOnNext(criteria -> log.info("--- Searching with criteria: {}", criteria))
                .flatMapMany(solicitudUseCase::searchSolicitudes)
                .collectList()
                .flatMap(solicitudes -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudes))
                .doOnSuccess(response -> log.info("<<< searchSolicitudes flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error during searchSolicitudes flow", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Ocurrió un error interno al buscar solicitudes."));
                });
    }
}
