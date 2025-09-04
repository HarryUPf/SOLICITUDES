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
public class SearchSolicitudRequestDTO {
    // Fields that can be used as search criteria.
    // These are all optional for a flexible search.
    private Integer id;
    private Integer idUser;
    private Integer dni;
    private BigDecimal monto;
    private LocalDate fechaPlazo;
    private Integer idTipoPrestamo;
    private EstadoSolicitud estado;
}