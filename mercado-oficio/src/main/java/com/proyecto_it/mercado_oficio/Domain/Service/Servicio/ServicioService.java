package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;

import java.util.List;

public interface ServicioService {
    Servicio crearServicio(Servicio servicio, String imagenUrl);
    Servicio actualizarServicio(Integer id, Servicio servicio, String imagenUrl);
    void eliminarServicio(Integer id, Integer usuarioId);
    Servicio obtenerServicioPorId(Integer id);
    List<Servicio> obtenerServiciosPorUsuario(Integer usuarioId);
    List<Servicio> obtenerServiciosPorOficio(Integer oficioId);
    List<Servicio> obtenerTodosLosServicios();
    void validarPermisos(Integer servicioId, Integer usuarioId);
}