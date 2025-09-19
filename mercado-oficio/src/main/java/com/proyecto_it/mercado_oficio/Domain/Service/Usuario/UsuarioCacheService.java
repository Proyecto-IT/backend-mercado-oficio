package com.proyecto_it.mercado_oficio.Domain.Service.Usuario;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioCacheService {

    private final UsuarioRepository usuarioRepository;

    //MÉTODOS DE LECTURA CON CACHE

    @Cacheable(value = "usuarios", key = "#gmail.toLowerCase()")
    public Optional<Usuario> buscarPorGmailCached(String gmail) {
        return usuarioRepository.buscarPorGmail(gmail);
    }

    @Cacheable(value = "usuariosPorId", key = "#id")
    public Optional<Usuario> buscarPorIdCached(int id) {
        return usuarioRepository.buscarPorId(id);
    }

    @Cacheable(value = "todosLosUsuarios")
    public List<Usuario> listarTodosCached() {
        return usuarioRepository.findAll();
    }

    @Cacheable(value = "existeUsuario", key = "#gmail.toLowerCase()")
    public boolean existePorGmailCached(String gmail) {
        return usuarioRepository.existePorGmail(gmail);
    }

    //MÉTODOS DE ACTUALIZACIÓN DE CACHE

    @Caching(
            put = {
                    @CachePut(value = "usuarios", key = "#usuario.gmail.toLowerCase()"),
                    @CachePut(value = "usuariosPorId", key = "#usuario.id")
            }
    )
    public Usuario actualizarUsuarioEnCache(Usuario usuario) {
        return usuario;
    }

    @CacheEvict(value = {"usuarios", "existeUsuario"}, key = "#gmail.toLowerCase()")
    public void evictUsuarioPorGmail(String gmail) {
        // Elimina usuario por gmail del cache
    }

    @CacheEvict(value = "usuariosPorId", key = "#id")
    public void evictUsuarioPorId(int id) {
        // Elimina usuario por id del cache
    }

    @CachePut(value = "todosLosUsuarios")
    public List<Usuario> actualizarListaCompleta(List<Usuario> usuarios) {
        return usuarios;
    }

    // Métodos de ayuda para mantener sincronizada la lista
    @CachePut(value = "todosLosUsuarios")
    public List<Usuario> agregarUsuarioALista(Usuario nuevo) {
        List<Usuario> lista = listarTodosCached();
        lista.add(nuevo);
        return lista;
    }

    @CachePut(value = "todosLosUsuarios")
    public List<Usuario> eliminarUsuarioDeLista(String gmail) {
        List<Usuario> lista = listarTodosCached();
        lista.removeIf(u -> u.getGmail().equalsIgnoreCase(gmail));
        return lista;
    }
}
