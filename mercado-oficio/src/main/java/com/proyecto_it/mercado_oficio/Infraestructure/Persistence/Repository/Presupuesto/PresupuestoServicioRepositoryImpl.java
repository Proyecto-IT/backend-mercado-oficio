package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.Model.PresupuestoServicio;
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
import org.springframework.transaction.annotation.Transactional;

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
            PresupuestoServicioEntity entity = mapper.toEntity(dto, servicio);
            PresupuestoServicioEntity guardado = jpaRepository.save(entity);
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

    @Override
    public PresupuestoServicioEntity getEntityById(Integer id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));
    }



    public PresupuestoServicioDTO getPresupuestoById(Integer id) {
        log.info("Buscando presupuesto con id={}", id);

        PresupuestoServicioDTO dto = jpaRepository.findDTOById(id)
                .orElseThrow(() -> {
                    log.error("Presupuesto con id={} no encontrado", id);
                    return new RuntimeException("Presupuesto no encontrado");
                });

        log.info("Presupuesto encontrado: {}", dto);
        return dto;
    }

    public PresupuestoServicioDTO getPresupuestoPrestadorById(Integer id) {
        return jpaRepository.findDTOByIdWithPrestador(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
    }

    @Override
    public List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente) {
        return jpaRepository.findByIdCliente(idCliente);
    }

    @Override
    @Transactional(readOnly = true)  // ✅ Agregar esto
    public List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador) {
        log.info("Obteniendo presupuestos del prestador: {}", idPrestador);

        List<PresupuestoServicioEntity> entities = jpaRepository
                .findByIdPrestador(idPrestador);

        return entities.stream()
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
    @Transactional(readOnly = true)  // ✅ Agregar esta anotación
    public List<PresupuestoServicioDTO> obtenerPorServicio(Integer servicioId) {
        log.info("Obteniendo presupuestos del servicio: {}", servicioId);

        List<PresupuestoServicioEntity> entities = jpaRepository.findByServicioId(servicioId);

        entities.forEach(entity -> {
            entity.getArchivos().size(); // Inicializa archivos
            entity.getHorariosSeleccionados().size(); // Inicializa horarios
        });

        return entities.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto) {
        PresupuestoServicioEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));

        entity.getHorariosSeleccionados().size();

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
