package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.*;
import co.com.bancolombia.api.mapper.SolicitudApiMapper;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import lombok.RequiredArgsConstructor;
import java.util.NoSuchElementException;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Handler {

    private static final String ERROR_LABEL = "error";

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    private final SolicitudUseCase solicitudUseCase;
    private final SolicitudApiMapper solicitudApiMapper;
    private final WebClient webClient;

    @PreAuthorize("hasRole('ADMIN') or hasRole('ASESOR') or hasRole('CLIENTE')")
    public Mono<ServerResponse> createSolicitud(ServerRequest serverRequest) {
        Mono<CreateSolicitudRequestDTO> requestDTOMono = serverRequest.bodyToMono(CreateSolicitudRequestDTO.class);

        return Mono.zip(requestDTOMono, ReactiveSecurityContextHolder.getContext())
                .doOnSubscribe(s -> log.info(">>> Starting createSolicitud flow"))
                .flatMap(tuple -> {
                    CreateSolicitudRequestDTO dto = tuple.getT1();
                    var securityContext = tuple.getT2();
                    var authentication = securityContext.getAuthentication();
                    boolean isCliente = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch("ROLE_CLIENTE"::equals);

                    if (isCliente) {
                        String userEmail = authentication.getName();
                        log.info("--- User is CLIENTE. Fetching ID for email: {}", userEmail);
                        String authorizationHeader = serverRequest.headers().firstHeader("Authorization");
                        return findUserByEmail(userEmail, authorizationHeader)
                                .map(user -> {
                                    dto.setIdUser(user.getId()); // Set user ID from external service
                                    dto.setEstado(null);
                                    return solicitudApiMapper.fromCreateDTO(dto);
                                });
                    } else {
                        log.info("--- User is ADMIN/ASESOR. Using idUser from request: {}", dto.getIdUser());
                        if (dto.getIdUser() == null) {
                            return Mono.error(new IllegalArgumentException("El 'idUser' es requerido para roles de Administrador o Asesor."));
                        }
                        return Mono.just(solicitudApiMapper.fromCreateDTO(dto));
                    }
                })
                .doOnNext(solicitud -> log.info("--- Creating solicitud model: {}", solicitud))
                .flatMap(solicitudUseCase::createSolicitud)
                .flatMap(solicitudGuardada -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudGuardada))
                .doOnSuccess(response -> log.info("<<< createSolicitud flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(Map.of(ERROR_LABEL, e.getMessage())))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error creando la solicitud", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Ocurrió un error interno al crear la solicitud."));
                });
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ASESOR')")
    public Mono<ServerResponse> searchSolicitudes(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SearchSolicitudRequestDTO.class)
                .doOnSubscribe(s -> log.info(">>> Starting searchSolicitudes flow"))
                .doOnNext(criteria -> log.info("--- Searching with criteria: {}", criteria))
                .map(solicitudApiMapper::fromSearchDTO)
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
                            .bodyValue(Map.of(ERROR_LABEL, "Ocurrió un error interno al buscar solicitudes."));
                });
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ASESOR')")
    public Mono<ServerResponse> searchSolicitudesSummary(ServerRequest serverRequest) {
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);

        return serverRequest.bodyToMono(SearchSolicitudRequestDTO.class)
                .doOnSubscribe(s -> log.info(">>> Starting searchSolicitudesSummary flow"))
                .doOnNext(criteria -> log.info("--- Searching summary with criteria: {}, page: {}, size: {}", criteria, page, size))
                .map(solicitudApiMapper::fromSearchDTO)
                .flatMapMany(solicitudUseCase::searchSolicitudes) // Returns Flux<Solicitud>
                .cache() // Cache the flux to allow multiple subscriptions (for count and for content)
                .as(flux -> {
                    // Get the content for the current page
                    Mono<List<SolicitudSummaryDTO>> pageContent = flux
                            .map(solicitudApiMapper::toSummaryDTO)
                            .skip((long) page * size)
                            .take(size)
                            .collectList();

                    // Get the total count of items
                    Mono<Long> totalItems = flux.count();

                    // Get the sum of the 'monto' field
                    Mono<Double> totalMonto = sumarMonto(flux);

                    // Zip them together to build the final paginated response
                    return Mono.zip(pageContent, totalItems, totalMonto)
                            .map(tuple -> {
                                List<SolicitudSummaryDTO> content = tuple.getT1();
                                Long count = tuple.getT2();
                                Double monto = tuple.getT3();
                                return PaginatedResponseDTO.<SolicitudSummaryDTO>builder()
                                        .content(content)
                                        .totalItems(count)
                                        .currentPage(page)
                                        .pageSize(size)
                                        .totalPages((int) Math.ceil((double) count / size))
                                        .totalMonto(monto)
                                        .build();
                            });
                })
                .flatMap(paginatedResponse -> ServerResponse.ok()
                        .bodyValue(paginatedResponse))
                .doOnSuccess(response -> log.info("<<< searchSolicitudesSummary flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error during searchSolicitudesSummary flow");
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(ERROR_LABEL, "Ocurrió un error interno al buscar los resúmenes de solicitudes."));
                });
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ASESOR')")
    public Mono<ServerResponse> updateEstadoSolicitud(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UpdateEstadoSolicitudDTO.class)
                .doOnSubscribe(s -> log.info(">>> Starting updateEstadoSolicitud flow"))
                .doOnNext(dto -> log.info("--- Updating estado with DTO: {}", dto))
                .flatMap(dto -> {
                    if (dto.getId() == null) {
                        return Mono.error(new IllegalArgumentException("El 'id' de la solicitud es requerido en el cuerpo de la petición."));
                    }

                    return solicitudUseCase.updateEstadoSolicitud(dto.getId(), dto.getEstado(), false);
                })
                .flatMap(solicitudActualizada -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudActualizada))
                .doOnSuccess(response -> log.info("<<< updateEstadoSolicitud flow completed successfully with status {}", response.statusCode()))
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(Map.of(ERROR_LABEL, e.getMessage())))
                .onErrorResume(NoSuchElementException.class, e -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(Map.of(ERROR_LABEL, e.getMessage())))
                .onErrorResume(Exception.class, e -> {
                    log.error("!!! Error updating estado for solicitud", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue(Map.of(ERROR_LABEL, "Ocurrió un error interno al actualizar la solicitud."));
                });
    }

    private Mono<UserDTO> findUserByEmail(String email, String authorizationHeader) {

        WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                .uri("/usuarios/buscar-por-email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email));

        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            requestSpec.header("Authorization", authorizationHeader);
        }
        return requestSpec
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnError(e -> log.error("!!! Failed to fetch user by email {}", email, e));
    }

    private Mono<Double> sumarMonto(Flux<Solicitud> solicitudesFlux) {
        return solicitudesFlux
                .reduce(0.0, (sum, solicitud) -> sum + (solicitud.getMonto() != null ? solicitud.getMonto().doubleValue() : 0.0));
    }
}
