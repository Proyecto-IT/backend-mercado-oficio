package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificacionHitoDTO {
    private Integer id;
    private Integer hitoId;
    private String descripcionCambio;
    private Double montoAnterior;
    private Double montoNuevo;
    private String fechaInicioAnterior;
    private String fechaIniciNueva;
    private String estadoAprobacion;
    private Boolean aprobadoCliente;
    private Boolean aprobadoPrestador;
    private String fechaCreacion;
}
