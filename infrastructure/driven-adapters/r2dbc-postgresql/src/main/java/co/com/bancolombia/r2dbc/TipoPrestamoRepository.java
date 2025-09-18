package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.data.TipoPrestamoData;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TipoPrestamoRepository extends ReactiveCrudRepository<TipoPrestamoData, Integer>, ReactiveQueryByExampleExecutor<TipoPrestamoData> {
}