package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;

import java.util.List;
import java.util.Optional;

public interface HitoRepository {
    Hito guardar(Hito hito);
    Optional<Hito> obtenerPorId(Integer id);
    List<Hito> obtenerPorPresupuesto(Integer presupuestoId);
    List<Hito> obtenerPorPresupuestoOrdenado(Integer presupuestoId);
    Long contarHitosPagados(Integer presupuestoId);
    void actualizar(Hito hito);
    void eliminar(Integer id);
    Integer obtenerClienteId(Integer hitoId);
    Integer obtenerPrestadorId(Integer hitoId);
    List<Hito> obtenerPorCliente(Integer clienteId);

}
