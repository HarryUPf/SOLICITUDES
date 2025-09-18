package co.com.bancolombia.model.tipoprestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TipoPrestamo {
    private Integer id;
    private Boolean validacionAutomatica;
}