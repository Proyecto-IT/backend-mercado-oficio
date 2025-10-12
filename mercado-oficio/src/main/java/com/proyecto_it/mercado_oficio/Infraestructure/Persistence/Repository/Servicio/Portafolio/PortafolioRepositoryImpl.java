package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.PortafolioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.JpaServicioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio.PortafolioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PortafolioRepositoryImpl implements PortafolioRepository {

    private final JpaPortafolioRepository jpaRepository;
    private final JpaServicioRepository servicioJpaRepository;
    private final PortafolioMapper mapper;

    @Override
    @Transactional
    public Portafolio save(Portafolio portafolio) {
        // Cargar la entidad Servicio
        ServicioEntity servicio = servicioJpaRepository.findById(portafolio.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        PortafolioEntity entity = mapper.toEntity(portafolio);
        entity.setServicio(servicio);

        PortafolioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Portafolio> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Portafolio> findByServicioId(Integer servicioId) {
        return jpaRepository.findByServicioId(servicioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllByServicioId(Integer servicioId) {
        jpaRepository.deleteAllByServicioId(servicioId);
    }
}
