package co.com.bancolombia.sqs.listener.dto;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateEstadoMessageDTO {
    private Integer id;
    private EstadoSolicitud estado;
}