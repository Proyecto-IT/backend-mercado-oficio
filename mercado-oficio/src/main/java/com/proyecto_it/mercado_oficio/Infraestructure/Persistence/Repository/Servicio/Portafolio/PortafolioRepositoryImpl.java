package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.PortafolioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.JpaServicioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio.PortafolioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PortafolioRepositoryImpl implements PortafolioRepository {

    private final JpaPortafolioRepository jpaRepository;
    private final PortafolioMapper mapper;
    private final JpaServicioRepository servicioJpaRepository;

    @Override
    public List<Portafolio> findByServicioId(Integer servicioId) {
        log.info("🔍 Buscando portafolios del servicio {}", servicioId);

        List<PortafolioEntity> entities = jpaRepository.findByServicioId(servicioId);

        log.info("✅ {} portafolios encontrados para servicio {}", entities.size(), servicioId);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Portafolio> findById(Integer id) {
        log.info("🔍 Buscando portafolio con ID {}", id);

        return jpaRepository.findById(id)
                .map(entity -> {
                    log.info("✅ Portafolio {} encontrado", id);
                    return mapper.toDomain(entity);
                });
    }

    @Override
    public Portafolio save(Portafolio portafolio) {
        try {
            log.info("💾 Guardando portafolio para servicio {}", portafolio.getServicioId());

            // Obtener la entidad del servicio
            ServicioEntity servicioEntity = servicioJpaRepository
                    .findById(portafolio.getServicioId())
                    .orElseThrow(() -> new RuntimeException(
                            "Servicio no encontrado: " + portafolio.getServicioId()));

            // Convertir a entity
            PortafolioEntity entity = mapper.toEntity(portafolio);
            entity.setServicio(servicioEntity);

            // Guardar
            PortafolioEntity savedEntity = jpaRepository.save(entity);

            log.info("✅ Portafolio {} guardado correctamente", savedEntity.getId());

            return mapper.toDomain(savedEntity);

        } catch (Exception e) {
            log.error("❌ Error al guardar portafolio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar portafolio: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        log.info("🗑️ Eliminando portafolio {}", id);

        if (!jpaRepository.existsById(id)) {
            throw new RuntimeException("Portafolio no encontrado: " + id);
        }

        jpaRepository.deleteById(id);
        log.info("✅ Portafolio {} eliminado", id);
    }

    @Override
    public boolean existsById(Integer id) {
        boolean exists = jpaRepository.existsById(id);
        log.info("🔍 Portafolio {} existe: {}", id, exists);
        return exists;
    }
}

