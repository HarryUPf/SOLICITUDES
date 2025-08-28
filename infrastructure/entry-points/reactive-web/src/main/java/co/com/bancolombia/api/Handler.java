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
import reactor.core.publisher.Flux;
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
        return serverRequest.bodyToMono(Solicitud.class)
                .doOnNext(solicitud -> log.info(">>> Petición para crear solicitud recibida: {}", solicitud))
                .flatMap(solicitudUseCase::createSolicitud)
                .flatMap(solicitudGuardada -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudGuardada))
                .doOnSuccess(response -> log.info("<<< Solicitud creada exitosamente"))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error creando la solicitud", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Error interno al crear la solicitud."));
                });
    }

//    public Mono<ServerResponse> getAllSolicitudes(ServerRequest serverRequest) {
//        log.info(">>> Petición para listar todas las solicitudes");
//        return ServerResponse.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(solicitudUseCase.getAllSolicitudes(), Solicitud.class);
//    }
//    public Mono<ServerResponse> getSolicitudById(ServerRequest serverRequest) {
//        String id = serverRequest.pathVariable("id");
//        log.info(">>> Petición para obtener solicitud con id: {}", id);
//        return solicitudUseCase.getSolicitudById(Integer.valueOf(id))
//                .flatMap(solicitud -> ServerResponse.ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(solicitud))
//                .switchIfEmpty(ServerResponse.notFound().build())
//                .doOnError(e -> log.error("!!! Error obteniendo solicitud por id: {}", id, e));
//    }
    public Mono<ServerResponse> findSolicitudes(ServerRequest request) {
        // This handler now checks for an 'id' query parameter.
        // If present, it fetches by ID. Otherwise, it returns all.
        return request.queryParam("id")
                .map(id -> {
                    log.info(">>> Petición para obtener solicitud con id (query param): {}", id);
                    return solicitudUseCase.getSolicitudById(Integer.valueOf(id))
                            .flatMap(solicitud -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitud))
                            .switchIfEmpty(ServerResponse.notFound().build());
                })
                .orElseGet(() -> {
                    log.info(">>> Petición para listar todas las solicitudes");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(solicitudUseCase.getAllSolicitudes(), Solicitud.class);
                });
    }

    public Mono<ServerResponse> searchSolicitudes(ServerRequest serverRequest) {
        Flux<Solicitud> foundSolicitudes = serverRequest.bodyToMono(Solicitud.class)
                .doOnSubscribe(subscription -> log.info(">>> Starting searchSolicitudes flow"))
                .doOnNext(criteria -> log.info("--- Search criteria received: {}", criteria))
                .flatMapMany(solicitudUseCase::searchSolicitudes)
                .doOnComplete(() -> log.info("<<< searchSolicitudes stream completed"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(foundSolicitudes, Solicitud.class)
                .doOnError(e -> log.error("!!! Error during searchSolicitudes flow", e))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", "Ocurrió un error interno inesperado.")));
    }
}
