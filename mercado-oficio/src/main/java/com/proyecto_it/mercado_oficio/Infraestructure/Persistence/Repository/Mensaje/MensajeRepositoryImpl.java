package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Repository.MensajeRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.MultimediaRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MensajeEntity;
import com.proyecto_it.mercado_oficio.Mapper.Mensaje.MensajeMapper;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MensajeRepositoryImpl implements MensajeRepository {

    private final JpaMensajeRepository jpaMensajeRepository;
    private final MultimediaRepository multimediaRepository;
    private final MensajeMapper mensajeMapper;

    @Override
    public Mensaje guardar(Mensaje mensaje) {
        MensajeEntity entity = mensajeMapper.toEntity(mensaje);
        MensajeEntity savedEntity = jpaMensajeRepository.save(entity);
        return mensajeMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Mensaje> findById(Long id) {
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
    public List<Multimedia> getArchivosMensaje(Long mensajeId) {
        log.info("Obteniendo archivos adjuntos del mensaje: {}", mensajeId);

        Mensaje mensaje = jpaMensajeRepository.findById(mensajeId)
                .map(mensajeMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado: " + mensajeId));


        if (mensaje.getMultimediaIds() == null || mensaje.getMultimediaIds().isEmpty()) {
            log.info("El mensaje no tiene archivos adjuntos");
            return new ArrayList<>();
        }

        List<Multimedia> archivos = multimediaRepository.findByIds(mensaje.getMultimediaIds());
        log.info("Se encontraron {} archivos adjuntos", archivos.size());

        return archivos;
    }

    @Override
    public void deleteById(Long id) {
        jpaMensajeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaMensajeRepository.existsById(id);
    }
}