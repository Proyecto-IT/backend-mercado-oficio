package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;

import java.util.List;
import java.util.Optional;

public interface MultimediaRepository {
    Multimedia subir(Multimedia multimedia);
    Optional<Multimedia> findById(Integer id);
    void deleteById(Integer id);
}