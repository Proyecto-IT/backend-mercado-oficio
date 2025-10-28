package com.proyecto_it.mercado_oficio.Mapper.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MultimediaDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MultimediaEntity;
import org.springframework.stereotype.Component;

@Component
public class MultimediaMapper {

    public Multimedia toDomain(MultimediaEntity entity) {
        if (entity == null) return null;

        return Multimedia.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .tipoContenido(entity.getTipoContenido())
                .extension(entity.getExtension())
                .datos(entity.getDatos())
                .build();
    }

    public MultimediaEntity toEntity(Multimedia domain) {
        if (domain == null) return null;

        return MultimediaEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .tipoContenido(domain.getTipoContenido())
                .extension(domain.getExtension())
                .datos(domain.getDatos())
                .build();
    }

    public MultimediaDTO toResponse(Multimedia domain) {
        if (domain == null) return null;

        return MultimediaDTO.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .tipoContenido(domain.getTipoContenido())
                .extension(domain.getExtension())
                .tamano(domain.getTamano())
                .urlDescarga("/api/multimedia/" + domain.getId() + "/descargar")
                .build();
    }
}