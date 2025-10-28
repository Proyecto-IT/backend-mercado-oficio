package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.ServicioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class ServicioRepositoryImpl implements ServicioRepository {

    private final JpaServicioRepository jpaRepository;
    private final JpaUsuarioRepository usuarioJpaRepository;
    private final ServicioMapper mapper;

    @Override
    public Optional<Servicio> findById(Integer id) {
        log.info("Buscando servicio por ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Servicio> findByIdWithDetails(Integer id) {
        log.info("Buscando servicio {} con detalles (usuario y oficio)", id);
        return jpaRepository.findByIdWithUsuario(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Servicio> findByUsuarioId(Integer usuarioId) {
        log.info("Buscando servicios del usuario {}", usuarioId);
        return jpaRepository.findByUsuarioIdWithUsuario(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Servicio> findByOficioId(Integer oficioId) {
        log.info("Buscando servicios del oficio {}", oficioId);
        return jpaRepository.findByOficioIdWithUsuario(oficioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Servicio> findAll() {
        log.info("Buscando todos los servicios");
        return jpaRepository.findAllWithUsuarios().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Servicio save(Servicio servicio) {
        try {
            log.info("Guardando servicio para usuario {}", servicio.getUsuarioId());

            UsuarioEntity usuarioEntity = usuarioJpaRepository.findById(servicio.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException(
                            "Usuario no encontrado: " + servicio.getUsuarioId()));

            ServicioEntity servicioEntity;

            if (servicio.getId() != null) {
                log.info("📝 Actualizando servicio existente con ID: {}", servicio.getId());
                servicioEntity = jpaRepository.findById(servicio.getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Servicio no encontrado: " + servicio.getId()));

                servicioEntity.setUsuario(usuarioEntity);
                servicioEntity.setOficioId(servicio.getOficioId());
                servicioEntity.setDescripcion(servicio.getDescripcion());
                servicioEntity.setTarifaHora(servicio.getTarifaHora() != null ?
                        servicio.getTarifaHora().toString() : null);
                servicioEntity.setDisponibilidad(mapper.serializeDisponibilidad(servicio.getDisponibilidad()));
                servicioEntity.setExperiencia(servicio.getExperiencia());
                servicioEntity.setEspecialidades(mapper.serializeEspecialidades(servicio.getEspecialidades()));
                servicioEntity.setUbicacion(servicio.getUbicacion());
                servicioEntity.setTrabajosCompletados(servicio.getTrabajosCompletados());

            } else {
                // Creación: nueva entity
                log.info("Creando nuevo servicio");
                servicioEntity = mapper.toEntity(servicio, usuarioEntity);
            }

            ServicioEntity servicioGuardado = jpaRepository.save(servicioEntity);

            log.info("Servicio guardado con ID: {}", servicioGuardado.getId());

            return mapper.toDomain(servicioGuardado);

        } catch (Exception e) {
            log.error("Error al guardar servicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar servicio: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        log.info("Eliminando servicio {}", id);

        if (!jpaRepository.existsById(id)) {
            throw new RuntimeException("Servicio no encontrado: " + id);
        }

        jpaRepository.deleteById(id);
        log.info("Servicio {} eliminado", id);
    }

    @Override
    public boolean existsById(Integer id) {
        boolean exists = jpaRepository.existsById(id);
        log.info("Servicio {} existe: {}", id, exists);
        return exists;
    }
}