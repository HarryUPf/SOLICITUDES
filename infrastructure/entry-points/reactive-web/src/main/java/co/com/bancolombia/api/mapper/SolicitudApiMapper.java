package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.CreateSolicitudRequestDTO;
import co.com.bancolombia.api.dto.SearchSolicitudRequestDTO;
import co.com.bancolombia.api.dto.SolicitudSummaryDTO;
import co.com.bancolombia.model.solicitud.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudApiMapper {

    @Mapping(target = "id", ignore = true)
    Solicitud fromCreateDTO(CreateSolicitudRequestDTO dto);

    Solicitud fromSearchDTO(SearchSolicitudRequestDTO dto);

    SolicitudSummaryDTO toSummaryDTO(Solicitud solicitud);
}