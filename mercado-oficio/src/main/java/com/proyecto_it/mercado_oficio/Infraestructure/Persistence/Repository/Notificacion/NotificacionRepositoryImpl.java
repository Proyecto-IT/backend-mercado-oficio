package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Notificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.Notificacion;
import com.proyecto_it.mercado_oficio.Domain.Repository.NotificacionRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Notificacion.NotificacionEntity;
import com.proyecto_it.mercado_oficio.Mapper.Notificacion.NotificacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class NotificacionRepositoryImpl implements NotificacionRepository {

    private final JpaNotificacionRepository jpaRepository;
    private final NotificacionMapper mapper;

    @Override
    public List<Notificacion> obtenerPorUsuario(Integer usuarioId) {
        return jpaRepository.findTop10ByUsuario_IdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        var usuario = new UsuarioEntity();
        usuario.setId(notificacion.getUsuarioId());
        var entity = mapper.toEntity(notificacion, usuario);
        entity = jpaRepository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public void marcarComoLeida(Integer notificacionId) {
        var entity = jpaRepository.findById(notificacionId).orElseThrow();
        entity.setLeida(true);
        jpaRepository.save(entity);
    }

    @Override
    public void eliminarPorId(Integer id) {
        jpaRepository.deleteById(id);
    }
}
