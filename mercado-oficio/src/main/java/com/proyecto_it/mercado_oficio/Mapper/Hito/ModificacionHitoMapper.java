package com.proyecto_it.mercado_oficio.Mapper.Hito;

import com.proyecto_it.mercado_oficio.Domain.Model.ModificacionHito;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.ModificacionHitoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.ModificacionHitoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ModificacionHitoMapper {
    public ModificacionHito toModel(ModificacionHitoEntity entity) {
        if (entity == null) return null;

        return new ModificacionHito(
                entity.getId(),
                entity.getHito() != null ? entity.getHito().getId() : null,
                entity.getDescripcionCambio(),
                entity.getMontoAnterior(),
                entity.getMontoNuevo(),
                entity.getFechaInicioAnterior(),
                entity.getFechaIniciNueva(),
                entity.getEstadoAprobacion(),
                entity.getAprobadoCliente(),
                entity.getAprobadoPrestador(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion()
        );
    }

    public ModificacionHitoEntity toEntity(ModificacionHito model) {
        if (model == null) return null;

        ModificacionHitoEntity entity = new ModificacionHitoEntity();
        entity.setId(model.getId());
        entity.setDescripcionCambio(model.getDescripcionCambio());
        entity.setMontoAnterior(model.getMontoAnterior());
        entity.setMontoNuevo(model.getMontoNuevo());
        entity.setFechaInicioAnterior(model.getFechaInicioAnterior());
        entity.setFechaIniciNueva(model.getFechaIniciNueva());
        entity.setEstadoAprobacion(model.getEstadoAprobacion());
        entity.setAprobadoCliente(model.getAprobadoCliente());
        entity.setAprobadoPrestador(model.getAprobadoPrestador());

        return entity;
    }

    public List<ModificacionHito> toModelList(List<ModificacionHitoEntity> entities) {
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public ModificacionHitoDTO toDTO(ModificacionHito model) {
        if (model == null) return null;

        return new ModificacionHitoDTO(
                model.getId(),
                model.getHitoId(),
                model.getDescripcionCambio(),
                model.getMontoAnterior().doubleValue(),
                model.getMontoNuevo().doubleValue(),
                model.getFechaInicioAnterior() != null ? model.getFechaInicioAnterior().toString() : null,
                model.getFechaIniciNueva() != null ? model.getFechaIniciNueva().toString() : null,
                model.getEstadoAprobacion() != null ? model.getEstadoAprobacion().toString() : null,
                model.getAprobadoCliente(),
                model.getAprobadoPrestador(),
                model.getFechaCreacion() != null ? model.getFechaCreacion().toString() : null
        );
    }

    public ModificacionHito toModel(ModificacionHitoDTO dto, Integer hitoId) {
        if (dto == null) return null;

        return new ModificacionHito(
                dto.getId(),
                hitoId,
                dto.getDescripcionCambio(),
                BigDecimal.valueOf(dto.getMontoAnterior()),
                BigDecimal.valueOf(dto.getMontoNuevo()),
                dto.getFechaInicioAnterior() != null ? LocalDateTime.parse(dto.getFechaInicioAnterior()) : null,
                dto.getFechaIniciNueva() != null ? LocalDateTime.parse(dto.getFechaIniciNueva()) : null,
                EstadoAprobacion.valueOf(dto.getEstadoAprobacion() != null ? dto.getEstadoAprobacion() : "PENDIENTE"),
                dto.getAprobadoCliente(),
                dto.getAprobadoPrestador(),
                dto.getFechaCreacion() != null ? LocalDateTime.parse(dto.getFechaCreacion()) : null,
                null
        );
    }

    public List<ModificacionHitoDTO> toDTOList(List<ModificacionHito> models) {
        return models.stream().map(this::toDTO).collect(Collectors.toList());
    }
}