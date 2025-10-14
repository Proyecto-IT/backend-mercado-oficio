package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository {
    Servicio save(Servicio servicio);
    Optional<Servicio> findById(Integer id);
    List<Servicio> findByUsuarioId(Integer usuarioId);
    List<Servicio> findByOficioId(Integer oficioId);
    List<Servicio> findAll();
    void deleteById(Integer id);
    Optional<Servicio> findByIdWithDetails(Integer id);
    boolean existsById(Integer id);

    }
