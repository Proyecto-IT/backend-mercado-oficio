package com.proyecto_it.mercado_oficio.Mapper.Presupuesto.PresupuestoArchivo;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoArchivo.PresupuestoArchivoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class PresupuestoServicioMapper {
    public PresupuestoServicioEntity toEntity(PresupuestoServicioDTO dto, Servicio servicio) {
        PresupuestoServicioEntity entity = new PresupuestoServicioEntity();
        entity.setIdCliente(dto.getIdCliente());
        entity.setIdPrestador(dto.getIdPrestador());
        entity.setDescripcionProblema(dto.getDescripcionProblema());
        entity.setHorasEstimadas(dto.getHorasEstimadas());
        entity.setCostoMateriales(dto.getCostoMateriales());
        entity.setDescripcionSolucion(dto.getDescripcionSolucion());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoPresupuesto.PENDIENTE);

        // Mapear Servicio modelo a ServicioEntity
        ServicioEntity servicioEntity = new ServicioEntity();
        servicioEntity.setId(servicio.getId());
        entity.setServicio(servicioEntity);

        return entity;
    }

    public PresupuestoServicioDTO toDTO(PresupuestoServicioEntity entity) {
        if (entity == null) return null;

        PresupuestoServicioDTO dto = new PresupuestoServicioDTO();
        dto.setId(entity.getId());
        dto.setServicioId(entity.getServicio().getId());
        dto.setIdCliente(entity.getIdCliente());
        dto.setIdPrestador(entity.getIdPrestador());
        dto.setDescripcionProblema(entity.getDescripcionProblema());
        dto.setHorasEstimadas(entity.getHorasEstimadas());
        dto.setCostoMateriales(entity.getCostoMateriales());
        dto.setPresupuesto(entity.getPresupuesto());
        dto.setDescripcionSolucion(entity.getDescripcionSolucion());
        dto.setEstado(entity.getEstado());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        dto.setRespondido(entity.getRespondido());

        if (entity.getArchivos() != null) {
            dto.setArchivos(entity.getArchivos().stream()
                    .map(this::archivoToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public PresupuestoServicioEntity toEntity(PresupuestoServicioCreateDTO dto, ServicioEntity servicio, Integer idPrestador) {
        PresupuestoServicioEntity entity = new PresupuestoServicioEntity();
        entity.setServicio(servicio);
        entity.setIdCliente(dto.getIdCliente());
        entity.setIdPrestador(idPrestador);
        entity.setDescripcionProblema(dto.getDescripcionProblema());
        entity.setEstado(EstadoPresupuesto.PENDIENTE);
        return entity;
    }

    public void updateEntity(PresupuestoServicioUpdateDTO dto, PresupuestoServicioEntity entity) {
        if (dto.getIdPrestador() != null) {
            entity.setIdPrestador(dto.getIdPrestador());
        }
        if (dto.getHorasEstimadas() != null) {
            entity.setHorasEstimadas(dto.getHorasEstimadas());
        }
        if (dto.getCostoMateriales() != null) {
            entity.setCostoMateriales(dto.getCostoMateriales());
        }
        if (dto.getDescripcionSolucion() != null) {
            entity.setDescripcionSolucion(dto.getDescripcionSolucion());
        }
        if (dto.getEstado() != null) {
            entity.setEstado(dto.getEstado());
        }
        entity.setRespondido(true);

    }

    public PresupuestoArchivoDTO archivoToDTO(PresupuestoArchivoEntity entity) {
        if (entity == null) return null;

        PresupuestoArchivoDTO dto = new PresupuestoArchivoDTO();
        dto.setId(entity.getId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setContenido(entity.getContenido());
        dto.setTipoMime(entity.getTipoMime());
        dto.setTipoArchivo(entity.getTipoArchivo());
        dto.setTamaniomB(entity.getTamanioMb());
        dto.setFechaCarga(entity.getFechaCarga());
        return dto;
    }

    public ServicioEntity servicioToEntity(Servicio servicio) {
        if (servicio == null) return null;
        ServicioEntity entity = new ServicioEntity();
        entity.setId(servicio.getId());
        return entity;
    }
}