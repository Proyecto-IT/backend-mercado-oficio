package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Hito;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;
import com.proyecto_it.mercado_oficio.Domain.Repository.HitoRepository;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.HitoEntity;
import com.proyecto_it.mercado_oficio.Mapper.Hito.HitoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class HitoRepositoryImpl implements HitoRepository {
    @Autowired
    private JpaHitoRepository jpaRepository;

    @Autowired
    private HitoMapper mapper;

    @Override
    public Hito guardar(Hito hito) {
        HitoEntity entity = mapper.toEntity(hito);
        HitoEntity guardado = jpaRepository.save(entity);
        return mapper.toModel(guardado);
    }

    @Override
    public Optional<Hito> obtenerPorId(Integer id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Integer obtenerClienteId(Integer hitoId) {
        return jpaRepository.obtenerClienteId(hitoId);
    }

    @Override
    public Integer obtenerPrestadorId(Integer hitoId) {
        return jpaRepository.obtenerPrestadorId(hitoId);
    }

    @Override
    public List<Hito> obtenerPorPresupuesto(Integer presupuestoId) {
        return jpaRepository.findByPresupuestoId(presupuestoId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Hito> obtenerPorPresupuestoOrdenado(Integer presupuestoId) {
        return jpaRepository.findByPresupuestoIdOrdenado(presupuestoId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Long contarHitosPagados(Integer presupuestoId) {
        return jpaRepository.countHitosPagados(presupuestoId, EstadoHito.PAGADO);
    }


    @Override
    public void actualizar(Hito hito) {
        HitoEntity entity = mapper.toEntity(hito);
        jpaRepository.save(entity);
    }

    @Override
    public void eliminar(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Hito> obtenerPorCliente(Integer clienteId) {
        List<HitoEntity> entities = jpaRepository.findByClienteId(clienteId);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }
}