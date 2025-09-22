package com.proyecto_it.mercado_oficio.Mapper.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Oficio.OficioEntity;
import org.springframework.stereotype.Component;

@Component
public class OficioMapper {
    public Oficio toDomain(OficioEntity entity) {
        return new Oficio(entity.getId(), entity.getNombre());
    }

    public OficioEntity toEntity(Oficio domain) {
        OficioEntity entity = new OficioEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        return entity;
    }
}