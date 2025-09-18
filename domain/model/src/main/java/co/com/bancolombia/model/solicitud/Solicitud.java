package co.com.bancolombia.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    private Integer id;
    private Integer idUser;
    private Integer dni;
    private BigDecimal monto;
    private LocalDate fechaPlazo;
    private Integer idTipoPrestamo;
    private BigDecimal tazaInteres;
    private EstadoSolicitud estado;

    private transient Boolean validacionAutomatica;

}