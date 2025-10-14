package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServicioService {
    Servicio crearServicio(Servicio servicio, MultipartFile imagen);
    Servicio crearServicioConPortafolios(Servicio servicio, MultipartFile imagen, List<Portafolio> portafolios);
    Servicio actualizarServicioConPortafolios(Integer id, Servicio servicio,
                                              MultipartFile imagen,
                                              List<Portafolio> portafolios);
    Servicio actualizarServicio(Integer id, Servicio servicioActualizado, MultipartFile imagen);    void eliminarServicio(Integer id, Integer usuarioId);
    Servicio obtenerServicioPorId(Integer id);
    List<Servicio> obtenerServiciosPorUsuario(Integer usuarioId);
    List<Servicio> obtenerServiciosPorOficio(Integer oficioId);
    List<Servicio> obtenerTodosLosServicios();
    void validarPermisos(Integer servicioId, Integer usuarioId);
}