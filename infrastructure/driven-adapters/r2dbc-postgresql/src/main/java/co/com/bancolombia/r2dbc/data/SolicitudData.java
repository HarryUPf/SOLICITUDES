package co.com.bancolombia.r2dbc.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("solicitud")
public class SolicitudData {

    @Id
    private Integer id;
    private Integer dni;
    private BigDecimal monto;
    @Column("fecha_plazo")
    private LocalDate fechaPlazo;
    @Column("tipo_prestamo_id")
    private Integer tipoPrestamoId;
    private String estado;
}