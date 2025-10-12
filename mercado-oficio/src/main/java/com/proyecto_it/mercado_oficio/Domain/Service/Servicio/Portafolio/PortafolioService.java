package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;

import java.util.List;

public interface PortafolioService {
    Portafolio crearPortafolio(Portafolio portafolio, Integer usuarioId);
    Portafolio actualizarPortafolio(Integer id, Portafolio portafolio, Integer usuarioId);
    void eliminarPortafolio(Integer id, Integer usuarioId);
    Portafolio obtenerPortafolioPorId(Integer id);
    List<Portafolio> obtenerPortafoliosPorServicio(Integer servicioId);
}
