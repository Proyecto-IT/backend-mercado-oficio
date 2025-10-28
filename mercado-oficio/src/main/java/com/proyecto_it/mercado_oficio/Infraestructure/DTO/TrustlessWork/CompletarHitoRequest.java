package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletarHitoRequest {
    private String evidence;  // Evidencia del trabajo (enlace, descripción, etc.)
}