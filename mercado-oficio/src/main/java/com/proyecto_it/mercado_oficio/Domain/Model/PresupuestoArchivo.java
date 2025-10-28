package com.proyecto_it.mercado_oficio.Domain.Model;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoArchivo {
    private Integer id;
    private String nombreArchivo;
    private byte[] contenido;
    private String tipoMime;
    private TipoArchivo tipoArchivo;
    private BigDecimal tamaniomB;
    private LocalDateTime fechaCarga;
}