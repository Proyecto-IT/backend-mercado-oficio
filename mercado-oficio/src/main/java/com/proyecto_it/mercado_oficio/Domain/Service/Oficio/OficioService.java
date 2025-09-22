package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;

import java.util.List;
import java.util.Optional;

public interface OficioService {
    Oficio crearOficio(Oficio oficio);
    List<Oficio> listarTodos();
    List<Oficio> buscarPorNombre(String nombre);
    Optional<Oficio> buscarPorId(Integer id);
    Oficio actualizarOficio(Oficio oficio);
    void eliminarOficio(Integer id);
}
