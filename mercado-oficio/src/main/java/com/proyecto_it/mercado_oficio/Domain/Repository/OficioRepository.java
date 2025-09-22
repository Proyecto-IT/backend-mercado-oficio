package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;

import java.util.List;
import java.util.Optional;

public interface OficioRepository {
    Oficio guardar(Oficio oficio);
    List<Oficio> findAll();
    List<Oficio> buscarPorNombre(String nombre);
    Optional<Oficio> buscarPorId(Integer id);
    Oficio actualizar(Oficio oficio);
    void eliminar(Integer id);
}
