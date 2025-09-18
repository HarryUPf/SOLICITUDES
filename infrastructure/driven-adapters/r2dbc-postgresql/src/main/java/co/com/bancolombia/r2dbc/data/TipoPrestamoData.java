package co.com.bancolombia.r2dbc.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("tipo_prestamo")
public class TipoPrestamoData {
    @Id
    private Integer id;
    @Column("validacion_automatica")
    private Boolean validacionAutomatica;
}