package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;

import java.util.List;

public interface PortafolioService {
    List<Portafolio> obtenerPortafoliosPorServicio(Integer servicioId);
    Portafolio crearPortafolio(Portafolio portafolio);
    Portafolio actualizarPortafolio(Integer id, Portafolio portafolio);
    void eliminarPortafolio(Integer id);
    Portafolio obtenerPortafolioPorId(Integer id);
}
