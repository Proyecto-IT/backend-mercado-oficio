package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioGetUpdateResponse {
    private String nombre;
    private String apellido;
    private String gmail;
    private String direccion;
    private String cp;      // código postal
    private String ciudad;
    private String telefono;
}

