package com.proyecto_it.mercado_oficio.Mapper.Hito;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.HitoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HitoMapper {
    public Hito toModel(HitoEntity entity) {
        if (entity == null) return null;

        return new Hito(
                entity.getId(),
                entity.getPresupuesto() != null ? entity.getPresupuesto().getId() : null,
                entity.getPorcentajePresupuesto(),
                entity.getMonto(),
                entity.getEstado(),
                entity.getFechaInicio(),
                entity.getFechaFinalizacionEstimada()
        );
    }

    public HitoEntity toEntity(Hito model) {
        if (model == null) return null;

        HitoEntity entity = new HitoEntity();
        entity.setId(model.getId());
        entity.setPorcentajePresupuesto(model.getPorcentajePresupuesto());
        entity.setMonto(model.getMonto());
        entity.setEstado(model.getEstado());
        entity.setFechaInicio(model.getFechaInicio());
        entity.setFechaFinalizacionEstimada(model.getFechaFinalizacionEstimada());
        if (model.getPresupuestoId() != null) {
            PresupuestoServicioEntity presupuesto = new PresupuestoServicioEntity();
            presupuesto.setId(model.getPresupuestoId());
            entity.setPresupuesto(presupuesto);
        }
        return entity;
    }

    public List<Hito> toModelList(List<HitoEntity> entities) {
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public HitoDTO toDTO(Hito model) {
        if (model == null) return null;

        return new HitoDTO(
                model.getId(),
                model.getPresupuestoId(),
                model.getPorcentajePresupuesto().doubleValue(),
                model.getMonto().doubleValue(),
                model.getEstado() != null ? model.getEstado().toString() : null,
                model.getFechaInicio() != null ? model.getFechaInicio().toString() : null,
                model.getFechaFinalizacionEstimada() != null ? model.getFechaFinalizacionEstimada().toString() : null
        );
    }

    public Hito toModel(HitoDTO dto, Integer presupuestoId) {
        if (dto == null) return null;

        return new Hito(
                dto.getId(),
                presupuestoId,
                BigDecimal.valueOf(dto.getPorcentajePresupuesto()),
                BigDecimal.valueOf(dto.getMonto()),
                EstadoHito.valueOf(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE"),
                dto.getFechaInicio() != null ? LocalDateTime.parse(dto.getFechaInicio()) : null,
                dto.getFechaFinalizacionEstimada() != null ? LocalDateTime.parse(dto.getFechaFinalizacionEstimada()) : null
        );
    }

    public List<HitoDTO> toDTOList(List<Hito> models) {
        return models.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
