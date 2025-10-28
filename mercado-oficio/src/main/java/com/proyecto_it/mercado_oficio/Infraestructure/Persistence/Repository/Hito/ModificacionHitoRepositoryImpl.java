package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Hito;

import com.proyecto_it.mercado_oficio.Domain.Model.ModificacionHito;
import com.proyecto_it.mercado_oficio.Domain.Repository.ModificacionHitoRepository;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.ModificacionHitoEntity;
import com.proyecto_it.mercado_oficio.Mapper.Hito.ModificacionHitoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ModificacionHitoRepositoryImpl implements ModificacionHitoRepository {
    @Autowired
    private JpaModificacionHitoRepository jpaRepository;

    @Autowired
    private ModificacionHitoMapper mapper;

    @Override
    public ModificacionHito guardar(ModificacionHito modificacion) {
        ModificacionHitoEntity entity = mapper.toEntity(modificacion);
        ModificacionHitoEntity guardado = jpaRepository.save(entity);
        return mapper.toModel(guardado);
    }

    @Override
    public Optional<ModificacionHito> obtenerPorId(Integer id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<ModificacionHito> obtenerPorHito(Integer hitoId) {
        return jpaRepository.findByHitoId(hitoId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModificacionHito> obtenerModificacionesPendientes(Integer hitoId) {
        return jpaRepository.findModificacionesPendientes(hitoId, EstadoAprobacion.PENDIENTE)
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }



    @Override
    public void actualizar(ModificacionHito modificacion) {
        ModificacionHitoEntity entity = mapper.toEntity(modificacion);
        jpaRepository.save(entity);
    }
}

