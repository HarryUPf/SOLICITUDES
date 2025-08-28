package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.solicitud.EstadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.r2dbc.data.SolicitudData;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@Repository
// TODO: Rename this file to MyReactiveRepositoryAdapter.java
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,       // Domain Model
        SolicitudData,   // Data Model
        Integer,         // ID type
        MyReactiveRepository // Spring Data R2DBC repository
        > implements SolicitudRepository {

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> Solicitud.builder()
                .id(d.getId())
                .dni(d.getDni())
                .monto(d.getMonto())
                .fechaPlazo(d.getFechaPlazo())
                .tipoPrestamoId(d.getTipoPrestamoId())
                .estado(Stream.of(EstadoSolicitud.values())
                        .filter(e -> e.getValue().equalsIgnoreCase(d.getEstado()))
                        .findFirst()
                        .orElse(null)) // Or throw an exception for data integrity
                .build());
    }

    @Override
    @Transactional
    public Mono<Solicitud> save(Solicitud entity) {
        // By annotating here, the transaction is managed at the infrastructure boundary
        // for this specific persistence operation.
        return super.save(entity);
    }

    @Override
    protected SolicitudData toData(Solicitud entity) {
        SolicitudData data = new SolicitudData();
        data.setId(entity.getId());
        data.setDni(entity.getDni());
        data.setMonto(entity.getMonto());
        data.setFechaPlazo(entity.getFechaPlazo());
        data.setTipoPrestamoId(entity.getTipoPrestamoId());
        if (entity.getEstado() != null) {
            // Map the enum to its string value for the database
            data.setEstado(entity.getEstado().getValue());
        }
        return data;
    }
}