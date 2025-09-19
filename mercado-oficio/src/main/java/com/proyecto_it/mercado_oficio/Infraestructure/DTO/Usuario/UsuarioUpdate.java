package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UsuarioUpdate {
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede tener más de 100 caracteres")
    private String apellido;

    @Size(max = 200, message = "La dirección no puede tener más de 200 caracteres")
    private String direccion;

    @Size(max = 20, message = "El código postal no puede tener más de 20 caracteres")
    @Pattern(regexp = "(^$|^[a-zA-Z0-9\\s-]{3,20}$)", message = "Código postal inválido")
    private String cp;

    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    private String ciudad;

    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    @Pattern(regexp = "(^$|\\d{1,20})", message = "El teléfono debe contener hasta 20 dígitos")
    private String telefono;
}
