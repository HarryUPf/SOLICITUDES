package co.com.bancolombia.model.tipoprestamo.gateways;

import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoGateway {
    Mono<TipoPrestamo> findById(Integer id);
}