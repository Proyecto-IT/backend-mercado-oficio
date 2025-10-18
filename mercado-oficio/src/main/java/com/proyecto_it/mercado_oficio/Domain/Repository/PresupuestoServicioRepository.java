package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;

import java.util.List;

public interface PresupuestoServicioRepository {
    PresupuestoServicioDTO guardar(PresupuestoServicioDTO dto, Servicio servicio);
    List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente);
    List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador);
    List<PresupuestoServicioDTO> obtenerPorEstado(EstadoPresupuesto estado);
    List<PresupuestoServicioDTO> obtenerPorServicio(Integer servicioId);
    PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto);
    void eliminar(Integer id);
    PresupuestoServicioDTO getPresupuestoById(Integer id);
    boolean estaRespondido(Integer presupuestoId);

}
