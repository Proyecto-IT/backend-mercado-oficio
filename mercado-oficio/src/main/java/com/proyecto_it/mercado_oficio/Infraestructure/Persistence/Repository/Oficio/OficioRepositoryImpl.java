package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Oficio.OficioEntity;
import com.proyecto_it.mercado_oficio.Mapper.Oficio.OficioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OficioRepositoryImpl implements OficioRepository {

    private final JpaOficioRepository jpaRepository;
    private final OficioMapper mapper;

    @Override
    public Oficio guardar(Oficio oficio) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(oficio)));
    }

    @Override
    public List<Oficio> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Oficio> buscarPorNombre(String nombre) {
        return jpaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Oficio> buscarPorId(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Oficio actualizar(Oficio oficio) {
        return guardar(oficio);
    }

    @Override
    public void eliminar(Integer id) {
        jpaRepository.deleteById(id);
    }
}