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

@Slf4j
@Repository
@RequiredArgsConstructor
public class ServicioRepositoryImpl implements ServicioRepository {

    private final JpaServicioRepository jpaRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final ServicioMapper mapper;
    private final OficioRepository oficioRepository;

    @Override
    public Servicio save(Servicio servicio) {
        log.info("💾 Guardando servicio para usuarioId={}", servicio.getUsuarioId());

        UsuarioEntity usuarioEntity = jpaUsuarioRepository.findById(servicio.getUsuarioId())
                .orElseThrow(() -> {
                    log.error("❌ Usuario con ID {} no encontrado", servicio.getUsuarioId());
                    return new RuntimeException("Usuario no encontrado con ID: " + servicio.getUsuarioId());
                });

        log.info("✅ Usuario encontrado: {}", usuarioEntity.getGmail());

        ServicioEntity entity = mapper.toEntity(servicio, usuarioEntity);

        log.info("🔍 Entity preparada: usuarioId={}, oficioId={}",
                entity.getUsuario().getId(), entity.getOficioId());

        ServicioEntity savedEntity = jpaRepository.save(entity);

        log.info("✅ Servicio guardado con ID={}", savedEntity.getId());

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Servicio> findById(Integer id) {
        log.info("🔍 Buscando servicio por ID={}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Servicio> findByIdWithDetails(Integer id) {
        log.info("🔍 Buscando servicio con detalles por ID={}", id);
        return jpaRepository.findByIdWithDetails(id) // asumir que JpaRepositorio tiene fetch join
                .map(mapper::toDomain);
    }

    @Override
    public List<Servicio> findByUsuarioId(Integer usuarioId) {
        log.info("🔍 Buscando servicios por usuarioId={}", usuarioId);
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Servicio> findByOficioId(Integer oficioId) {
        log.info("🔍 Buscando servicios por oficioId={}", oficioId);
        return jpaRepository.findByOficioId(oficioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Servicio> findAll() {
        log.info("🔍 Buscando todos los servicios");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        log.info("🗑️ Eliminando servicio con ID={}", id);
        jpaRepository.deleteById(id);
        log.info("✅ Servicio eliminado");
    }

    @Override
    public boolean existsById(Integer id) {
        boolean exists = jpaRepository.existsById(id);
        log.info("❔ Verificando existencia servicio ID={} => {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsByUsuarioId(Integer usuarioId) {
        boolean exists = jpaRepository.existsByUsuarioId(usuarioId);
        log.info("❔ Verificando existencia servicios para usuarioId={} => {}", usuarioId, exists);
        return exists;
    }
}
