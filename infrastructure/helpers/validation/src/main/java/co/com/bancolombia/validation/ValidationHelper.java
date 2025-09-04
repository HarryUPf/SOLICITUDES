package co.com.bancolombia.validation;

import co.com.bancolombia.model.solicitud.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationHelper {

    public Mono<Solicitud> validateSolicitud(Solicitud solicitud) {
        List<String> errors = new ArrayList<>();

        if (solicitud.getDni() == null) {
            errors.add("El campo 'DNI' no puede ser nulo");
        } else if (solicitud.getDni() <= 0) {
            errors.add("El campo 'DNI' debe ser un número positivo");
        }

        if (solicitud.getMonto() == null) {
            errors.add("El campo 'Monto' no puede ser nulo");
        } else if (solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("El campo 'Monto' debe ser un valor positivo");
        }

        if (solicitud.getFechaPlazo() == null) {
            errors.add("El campo 'Fecha de Plazo' no puede ser nulo");
        } else if (!solicitud.getFechaPlazo().isAfter(LocalDate.now())) {
            errors.add("El campo 'Fecha de Plazo' debe ser una fecha futura");
        }

        if (solicitud.getIdTipoPrestamo() == null) {
            errors.add("El campo 'ID Tipo de Préstamo' no puede ser nulo");
        }
//        else if (solicitud.getIdTipoPrestamo() <= 0) {
//            errors.add("El campo 'ID Tipo de Préstamo' debe ser un número positivo");
//        }

        if (errors.isEmpty()) {
            return Mono.just(solicitud);
        }

        String errorMessage = "Error de validación: " + String.join(". ", errors) + ".";
        return Mono.error(new IllegalArgumentException(errorMessage));
    }
}