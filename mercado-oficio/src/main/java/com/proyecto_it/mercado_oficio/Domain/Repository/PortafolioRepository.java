package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;

import java.util.List;
import java.util.Optional;

public interface PortafolioRepository {
    List<Portafolio> findByServicioId(Integer servicioId);
    Optional<Portafolio> findById(Integer id);
    Portafolio save(Portafolio portafolio);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}