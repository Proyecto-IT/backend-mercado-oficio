package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Repository.MensajeRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MensajeEntity;
import com.proyecto_it.mercado_oficio.Mapper.Mensaje.MensajeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MensajeRepositoryImpl implements MensajeRepository {

    private final JpaMensajeRepository jpaMensajeRepository;
    private final MensajeMapper mensajeMapper;

    @Override
    public Mensaje save(Mensaje mensaje) {
        MensajeEntity entity = mensajeMapper.toEntity(mensaje);
        MensajeEntity savedEntity = jpaMensajeRepository.save(entity);
        return mensajeMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Mensaje> findById(Integer id) {
        return jpaMensajeRepository.findById(id)
                .map(mensajeMapper::toDomain);
    }

    @Override
    public List<Mensaje> findByChat(Integer usuario1Id, Integer usuario2Id) {
        return jpaMensajeRepository.findByChat(usuario1Id, usuario2Id)
                .stream()
                .map(mensajeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        jpaMensajeRepository.deleteById(id);
    }
}