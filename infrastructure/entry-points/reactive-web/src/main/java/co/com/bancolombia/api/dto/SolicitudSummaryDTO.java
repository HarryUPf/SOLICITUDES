package co.com.bancolombia.api.dto;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSummaryDTO {
    private Integer idUser;
    private BigDecimal monto;
    private LocalDate fechaPlazo;
//    private String email = "cliente@example.com";
//    private String fullName = "Joe Doe";
    private Integer idTipoPrestamo;
    private BigDecimal tazaInteres;
    private EstadoSolicitud estado;
//    private BigDecimal salarioBase = BigDecimal.valueOf(50000.00);
}
