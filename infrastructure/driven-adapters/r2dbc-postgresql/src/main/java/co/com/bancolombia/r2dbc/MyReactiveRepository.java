package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.data.SolicitudData;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MyReactiveRepository extends ReactiveCrudRepository<SolicitudData, Integer>, ReactiveQueryByExampleExecutor<SolicitudData> {
}