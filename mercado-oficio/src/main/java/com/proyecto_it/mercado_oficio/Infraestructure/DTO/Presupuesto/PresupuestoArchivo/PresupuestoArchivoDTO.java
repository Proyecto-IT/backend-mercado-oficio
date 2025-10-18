package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoArchivoDTO {
    private Integer id;
    private String nombreArchivo;
    @JsonIgnore
    private byte[] contenido;
    private String tipoMime;
    private TipoArchivo tipoArchivo;
    private BigDecimal tamaniomB;
    private LocalDateTime fechaCarga;

    // Constructor para la query
    public PresupuestoArchivoDTO(Integer id, String nombreArchivo, String tipoMime,
                                 TipoArchivo tipoArchivo, BigDecimal tamaniomB, LocalDateTime fechaCarga) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.tipoMime = tipoMime;
        this.tipoArchivo = tipoArchivo;
        this.tamaniomB = tamaniomB;
        this.fechaCarga = fechaCarga;
    }
}
