package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortafolioServiceImpl implements PortafolioService {

    private final PortafolioRepository portafolioRepository;
    private final ServicioRepository servicioRepository;

    @Override
    @Transactional
    public Portafolio crearPortafolio(Portafolio portafolio, Integer usuarioId) {
        portafolio.validar();

        // Validar que el servicio existe y pertenece al usuario
        validarPropietarioServicio(portafolio.getServicioId(), usuarioId);

        return portafolioRepository.save(portafolio);
    }

    @Override
    @Transactional
    public Portafolio actualizarPortafolio(Integer id, Portafolio portafolioActualizado, Integer usuarioId) {
        Portafolio portafolioExistente = portafolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado con ID: " + id));

        // Validar que el servicio pertenece al usuario
        validarPropietarioServicio(portafolioExistente.getServicioId(), usuarioId);

        // Construir portafolio actualizado
        Portafolio portafolioFinal = Portafolio.builder()
                .id(id)
                .servicioId(portafolioExistente.getServicioId())
                .titulo(portafolioActualizado.getTitulo() != null ?
                        portafolioActualizado.getTitulo() : portafolioExistente.getTitulo())
                .descripcion(portafolioActualizado.getDescripcion() != null ?
                        portafolioActualizado.getDescripcion() : portafolioExistente.getDescripcion())
                .build();

        portafolioFinal.validar();

        return portafolioRepository.save(portafolioFinal);
    }

    @Override
    @Transactional
    public void eliminarPortafolio(Integer id, Integer usuarioId) {
        Portafolio portafolio = portafolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado con ID: " + id));

        validarPropietarioServicio(portafolio.getServicioId(), usuarioId);

        portafolioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Portafolio obtenerPortafolioPorId(Integer id) {
        return portafolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Portafolio> obtenerPortafoliosPorServicio(Integer servicioId) {
        return portafolioRepository.findByServicioId(servicioId);
    }

    private void validarPropietarioServicio(Integer servicioId, Integer usuarioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (!servicio.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para modificar portafolios de este servicio");
        }
    }
}
