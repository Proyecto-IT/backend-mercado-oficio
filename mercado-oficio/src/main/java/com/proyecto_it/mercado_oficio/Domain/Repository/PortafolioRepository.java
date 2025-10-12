package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;

import java.util.List;
import java.util.Optional;

public interface PortafolioRepository {
    Portafolio save(Portafolio portafolio);
    Optional<Portafolio> findById(Integer id);
    List<Portafolio> findByServicioId(Integer servicioId);
    void deleteById(Integer id);
    void deleteAllByServicioId(Integer servicioId);
}
