package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.PresupuestoServicio;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Exception.ValidationException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;

import java.util.List;
public interface PresupuestoServicioService {
    PresupuestoServicioDTO crear(PresupuestoServicioCreateDTO dto);
    PresupuestoServicioDTO obtener(Integer id);
    List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente);
    List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador);
    List<PresupuestoServicioDTO> obtenerPorEstado(EstadoPresupuesto estado);
    PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto);
    void eliminar(Integer id);
    PresupuestoArchivoDTO cargarArchivo(Integer presupuestoId, PresupuestoArchivoCreateDTO dto) throws ValidationException;
    List<PresupuestoArchivoDTO> obtenerArchivos(Integer presupuestoId);
    void eliminarArchivo(Integer archivoId);
    PresupuestoArchivoDTO obtenerPorId(Integer id);
    boolean estaRespondido(Integer presupuestoId);
    PresupuestoServicioDTO actualizarEstado(Integer id, EstadoPresupuesto nuevoEstado);
    List<PresupuestoServicioDTO> obtenerPorServicio(Integer servicioId);

}
