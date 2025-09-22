package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Oficio;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Oficio.OficioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOficioRepository extends JpaRepository<OficioEntity, Integer> {
    List<OficioEntity> findByNombreContainingIgnoreCase(String nombre);
}