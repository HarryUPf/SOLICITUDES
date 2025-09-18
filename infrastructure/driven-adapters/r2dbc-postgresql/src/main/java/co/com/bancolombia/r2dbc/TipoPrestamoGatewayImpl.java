package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoGateway;
import co.com.bancolombia.r2dbc.data.TipoPrestamoData;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TipoPrestamoGatewayImpl extends ReactiveAdapterOperations<
        TipoPrestamo,
        TipoPrestamoData,
        Integer,
        TipoPrestamoRepository
        > implements TipoPrestamoGateway {

    public TipoPrestamoGatewayImpl(TipoPrestamoRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, TipoPrestamo.class));
    }

    @Override
    public Mono<TipoPrestamo> findById(Integer id) {
        return repository.findById(id)
                .map(this::toEntity);
    }
}