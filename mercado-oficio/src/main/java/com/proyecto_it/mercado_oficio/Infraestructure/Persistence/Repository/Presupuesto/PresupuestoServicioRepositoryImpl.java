package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Mapper.Presupuesto.PresupuestoArchivo.PresupuestoServicioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class PresupuestoServicioRepositoryImpl implements PresupuestoServicioRepository {

    @Autowired
    private JpaPresupuestoServicioRepository jpaRepository;

    @Autowired
    private PresupuestoServicioMapper mapper;

    @Override
    public PresupuestoServicioDTO guardar(PresupuestoServicioDTO dto, Servicio servicio) {
        try {
            // Mapear solo internamente a entity para persistir
            PresupuestoServicioEntity entity = mapper.toEntity(dto, servicio);
            PresupuestoServicioEntity guardado = jpaRepository.save(entity);

            // Mapear de nuevo a DTO para devolver
            return mapper.toDTO(guardado);

        } catch (Exception e) {
            log.error("Error al guardar presupuesto", e);
            throw new RuntimeException("Error al guardar el presupuesto: " + e.getMessage());
        }
    }

    @Override
    public boolean estaRespondido(Integer presupuestoId) {
        return jpaRepository.findByIdRespondido(presupuestoId).isPresent();
    }

    public PresupuestoServicioDTO getPresupuestoById(Integer id) {
        return jpaRepository.findDTOById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
    }

    @Override
    public List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente) {
        return jpaRepository.findByIdCliente(idCliente);
    }

    @Override
    public List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador) {
        return jpaRepository.findByIdPrestador(idPrestador).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PresupuestoServicioDTO> obtenerPorEstado(EstadoPresupuesto estado) {
        return jpaRepository.findByEstado(estado).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PresupuestoServicioDTO> obtenerPorServicio(Integer servicioId) {
        return jpaRepository.findByServicioId(servicioId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto) {
        PresupuestoServicioEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));

        mapper.updateEntity(dto, entity);
        PresupuestoServicioEntity actualizado = jpaRepository.save(entity);
        return mapper.toDTO(actualizado);
    }

    @Override
    public void eliminar(Integer id) {
        PresupuestoServicioEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));
        jpaRepository.delete(entity);
    }
}
