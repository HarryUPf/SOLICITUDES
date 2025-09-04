package co.com.bancolombia.api.dto;

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
public class CreateSolicitudRequestDTO {
    private Integer idUser;
    private Integer dni;
    private BigDecimal monto;
    private LocalDate fechaPlazo;
    private Integer idTipoPrestamo;
    private String estado;
}