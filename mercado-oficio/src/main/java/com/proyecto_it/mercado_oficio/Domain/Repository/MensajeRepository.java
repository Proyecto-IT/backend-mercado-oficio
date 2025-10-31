package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;

import java.util.List;
import java.util.Optional;

public interface MensajeRepository {
    Mensaje guardar(Mensaje mensaje);
    Optional<Mensaje> findById(Long id);
    List<Mensaje> findByChat(Integer emisorId, Integer receptorId);
    List<Multimedia> getArchivosMensaje(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}