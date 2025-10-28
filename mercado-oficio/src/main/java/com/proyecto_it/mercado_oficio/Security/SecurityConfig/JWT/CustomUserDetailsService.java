package com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String gmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String rol = "";
        if (usuario.getPermiso()==0){
            rol = "CLIENTE";
        } else if (usuario.getPermiso()==1) {
            rol = "ADMIN";
        } else if (usuario.getPermiso()==2) {
            rol = "TRABAJADOR";

        }

        return User.builder()
                .username(usuario.getGmail())
                .password(usuario.getPassword())
                .roles(rol)
                .build();
    }
}



