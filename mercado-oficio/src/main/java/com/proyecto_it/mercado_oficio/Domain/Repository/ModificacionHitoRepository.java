package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.ModificacionHito;

import java.util.List;
import java.util.Optional;

public interface ModificacionHitoRepository {
    ModificacionHito guardar(ModificacionHito modificacion);
    Optional<ModificacionHito> obtenerPorId(Integer id);
    List<ModificacionHito> obtenerPorHito(Integer hitoId);
    List<ModificacionHito> obtenerModificacionesPendientes(Integer hitoId);
    void actualizar(ModificacionHito modificacion);
}
