package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud sampleSolicitud;

    @BeforeEach
    void setUp() {
        sampleSolicitud = Solicitud.builder()
                .id(1)
                .idUser(101)
                .dni(12345678)
                .monto(new BigDecimal("50000.00"))
                .fechaPlazo(LocalDate.now().plusYears(5))
                .idTipoPrestamo(1)
                .monto(new BigDecimal("5"))
                .estado(EstadoSolicitud.PENDIENTE)
                .build();
    }

    @Test
    @DisplayName("Should create a solicitud successfully")
    void createSolicitud_Success() {
        // Arrange
        Solicitud solicitudToCreate = sampleSolicitud.toBuilder().id(null).build();
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(sampleSolicitud));

        // Act
        Mono<Solicitud> result = solicitudUseCase.createSolicitud(solicitudToCreate);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(created ->
                        created.getId().equals(1) &&
                                created.getEstado().equals(EstadoSolicitud.PENDIENTE) &&
                                created.getIdUser().equals(101)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an error when creating a solicitud fails")
    void createSolicitud_Error() {
        // Arrange
        Solicitud solicitudToCreate = sampleSolicitud.toBuilder().id(null).build();
        when(solicitudRepository.save(any(Solicitud.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        Mono<Solicitud> result = solicitudUseCase.createSolicitud(solicitudToCreate);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database error")
                )
                .verify();
    }

}