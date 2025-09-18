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
    @Column("id_user")
    private Integer idUser;
    private Integer dni;
    private BigDecimal monto;
    @Column("fecha_plazo")
    private LocalDate fechaPlazo;
    @Column("id_tipo_prestamo")
    private Integer idTipoPrestamo;
    @Column("taza_interes")
    private BigDecimal tazaInteres;
    private String estado;
}