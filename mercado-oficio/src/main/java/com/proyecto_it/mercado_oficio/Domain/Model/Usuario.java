package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    private Integer id;
    private int permiso;
    private String nombre;
    private String apellido;
    private String gmail;
    private String password;
    private boolean verificado = false;
    private String proveedor; // LOCAL, GOOGLE, GOOGLE,LOCAL
    private String direccion;
    private String cp;
    private String ciudad;
    private String telefono;
    @ToString.Exclude // Evitar imprimir bytes en logs
    private byte[] imagen; // Almacenar imagen como BLOB
    private String imagenTipo;    // Métodos de dominio

    public boolean esAdministrador() {
        return permiso == 1;
    }

    public boolean esCliente() {
        return permiso == 0;
    }

    public boolean esTrabajador() {
        return permiso == 2;
    }

    public String getRolString() {
        return switch (permiso) {
            case 0 -> "CLIENTE";
            case 1 -> "ADMIN";
            case 2 -> "TRABAJADOR";
            default -> throw new IllegalArgumentException("Permiso inválido: " + permiso);
        };
    }

    public boolean puedeUsarGoogle() {
        return proveedor != null && proveedor.contains("GOOGLE");
    }

    public boolean puedeUsarLocal() {
        return proveedor != null && proveedor.contains("LOCAL");
    }

    public boolean esUsuarioHibrido() {
        return puedeUsarGoogle() && puedeUsarLocal();
    }

    public void actualizarProveedor(String nuevoProveedor) {
        if (this.proveedor == null || this.proveedor.trim().isEmpty()) {
            this.proveedor = nuevoProveedor;
        } else if (!this.proveedor.contains(nuevoProveedor)) {
            this.proveedor = this.proveedor + "," + nuevoProveedor;
        }
    }

    public boolean tienePasswordLocal() {
        // Un password válido no debe ser el placeholder de Google
        return password != null && !password.isEmpty() && !isGooglePlaceholderPassword();
    }

    private boolean isGooglePlaceholderPassword() {
        // Detectar si es un password placeholder generado para usuarios de Google
        return password != null && password.startsWith("$2") && password.length() > 50; // BCrypt hash
    }

    public void establecerPasswordLocal(String nuevaPassword) {
        this.password = nuevaPassword;
        actualizarProveedor("LOCAL");
    }

    public void vincularGoogle() {
        actualizarProveedor("GOOGLE");
        if (!verificado) {
            setVerificado(true); // Google ya verifica emails
        }
    }
    public boolean tieneImagen() {
        return imagen != null && imagen.length > 0;
    }

    public long getTamanoImagen() {
        return imagen != null ? imagen.length : 0;
    }

    // Limpiar imagen (útil para eliminar)
    public void eliminarImagen() {
        this.imagen = null;
        this.imagenTipo = null;
    }
    public void validar() {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (gmail == null || gmail.isBlank()) {
            throw new IllegalArgumentException("El Gmail es obligatorio.");
        }
        if (password != null && (password.length() < 8 || password.length() > 16)) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 16 caracteres.");
        }
    }
}
