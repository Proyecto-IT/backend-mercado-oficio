package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Mensaje.Multimedia;

import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Repository.MultimediaRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MultimediaEntity;
import com.proyecto_it.mercado_oficio.Mapper.Mensaje.MultimediaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MultimediaRepositoryImpl implements MultimediaRepository {

    private final JpaMultimediaRepository jpaMultimediaRepository;
    private final MultimediaMapper multimediaMapper;

    @Override
    public Multimedia guardar(Multimedia multimedia) {
        MultimediaEntity entity = multimediaMapper.toEntity(multimedia);
        MultimediaEntity savedEntity = jpaMultimediaRepository.save(entity);
        return multimediaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Multimedia> findById(Integer id) {
        return jpaMultimediaRepository.findById(id)
                .map(multimediaMapper::toDomain);
    }

    @Override
    public List<Multimedia> findByIds(List<Integer> ids) {
        return jpaMultimediaRepository.findByIds(ids)
                .stream()
                .map(multimediaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        jpaMultimediaRepository.deleteById(id);
    }
}