package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;

import java.util.List;

public interface PresupuestoArchivoRepository {
    PresupuestoArchivoDTO guardar(Integer presupuestoId, PresupuestoArchivoCreateDTO dto);
    PresupuestoArchivoDTO obtenerPorId(Integer id);
    List<PresupuestoArchivoDTO> obtenerPorPresupuesto(Integer presupuestoId);
    void eliminar(Integer id);
    long contarPorTipo(Integer presupuestoId, TipoArchivo tipoArchivo);
    List<PresupuestoArchivoDTO> getArchivosByPresupuestoId(Integer id);
}