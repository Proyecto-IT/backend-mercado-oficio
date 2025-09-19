package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEmailRequest {
    private String nuevoEmail;
}
