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
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudData,
        Integer,
        MyReactiveRepository
        > implements SolicitudRepository {

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> Solicitud.builder()
                .id(d.getId())
                .idUser(d.getIdUser())
                .dni(d.getDni())
                .monto(d.getMonto())
                .fechaPlazo(d.getFechaPlazo())
                .idTipoPrestamo(d.getIdTipoPrestamo())
                .tazaInteres(d.getTazaInteres())
                .estado(Stream.of(EstadoSolicitud.values())
                        .filter(e -> e.getValue().equalsIgnoreCase(d.getEstado()))
                        .findFirst()
                        .orElse(null))
                .build());
    }

    @Override
    @Transactional
    public Mono<Solicitud> save(Solicitud entity) {
        return super.save(entity);
    }

    @Override
    protected SolicitudData toData(Solicitud entity) {
        SolicitudData data = new SolicitudData();
        data.setId(entity.getId());
        data.setIdUser(entity.getIdUser());
        data.setDni(entity.getDni());
        data.setMonto(entity.getMonto());
        data.setFechaPlazo(entity.getFechaPlazo());
        data.setIdTipoPrestamo(entity.getIdTipoPrestamo());
        data.setTazaInteres(entity.getTazaInteres());
        if (entity.getEstado() != null) {
            data.setEstado(entity.getEstado().getValue());
        }
        return data;
    }
}