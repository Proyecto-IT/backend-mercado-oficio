package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.PresupuestoServicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoArchivoRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.ServicioService;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Exception.ValidationException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Mapper.Presupuesto.PresupuestoArchivo.PresupuestoServicioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PresupuestoServicioServiceImpl implements PresupuestoServicioService {
    @Autowired
    private PresupuestoServicioRepository presupuestoRepository;

    @Autowired
    private PresupuestoArchivoRepository archivoRepository;

    @Autowired
    private PresupuestoServicioMapper mapper;

    @Autowired
    private ServicioService servicioService;

    public PresupuestoServicioDTO crear(PresupuestoServicioCreateDTO dto) {
        try {
            // 1️⃣ Obtener el modelo Servicio
            Servicio servicio = servicioService.obtenerServicioPorId(dto.getServicioId());

            // 2️⃣ Convertir CreateDTO a DTO normal
            PresupuestoServicioDTO presupuestoDTO = new PresupuestoServicioDTO();
            presupuestoDTO.setIdCliente(dto.getIdCliente());
            presupuestoDTO.setDescripcionProblema(dto.getDescripcionProblema());
            // si tenés estado por defecto
            presupuestoDTO.setEstado(EstadoPresupuesto.PENDIENTE);

            // 3️⃣ Guardar usando el repository que trabaja con modelos
            return presupuestoRepository.guardar(presupuestoDTO, servicio);
        } catch (Exception e) {
            log.error("Error al crear presupuesto", e);
            throw new RuntimeException("No se pudo crear el presupuesto: " + e.getMessage());
        }
    }

    @Override
    public boolean estaRespondido(Integer presupuestoId) {
        return presupuestoRepository.estaRespondido(presupuestoId);
    }

    public PresupuestoServicioDTO obtener(Integer id) {
        return presupuestoRepository.getPresupuestoById(id);
    }

    public List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente) {
        return presupuestoRepository.obtenerPorCliente(idCliente);
    }

    public List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador) {
        return presupuestoRepository.obtenerPorPrestador(idPrestador);
    }

    public List<PresupuestoServicioDTO> obtenerPorEstado(EstadoPresupuesto estado) {
        return presupuestoRepository.obtenerPorEstado(estado);
    }

    public PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto) {
        return presupuestoRepository.actualizar(id, dto);
    }

    public void eliminar(Integer id) {
        presupuestoRepository.eliminar(id);
    }

    public PresupuestoArchivoDTO cargarArchivo(Integer presupuestoId, PresupuestoArchivoCreateDTO dto) throws ValidationException {
        return archivoRepository.guardar(presupuestoId, dto);
    }

    public List<PresupuestoArchivoDTO> obtenerArchivos(Integer presupuestoId) {
        return archivoRepository.obtenerPorPresupuesto(presupuestoId);
    }
    @Override
    public PresupuestoArchivoDTO obtenerPorId(Integer id) {
        // Suponiendo que ya tenés un método en el repositorio que devuelve DTO por ID
        return archivoRepository.obtenerPorId(id);
    }
    public void eliminarArchivo(Integer archivoId) {
        archivoRepository.eliminar(archivoId);
    }
}