package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequest {
    @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido puede tener hasta 100 caracteres")
    private String apellido;

    @Size(max = 255, message = "La dirección puede tener hasta 255 caracteres")
    private String direccion;

    @Size(max = 10, message = "El código postal puede tener hasta 10 caracteres")
    private String cp;

    @Size(max = 100, message = "La ciudad puede tener hasta 100 caracteres")
    private String ciudad;

    @Size(max = 20, message = "El teléfono puede tener hasta 20 caracteres")
    private String telefono;
}
