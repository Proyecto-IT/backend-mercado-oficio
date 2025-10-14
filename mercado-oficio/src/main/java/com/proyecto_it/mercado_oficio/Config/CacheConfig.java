package com.proyecto_it.mercado_oficio.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // ✅ Configurar Caffeine correctamente (sin build)
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats());

        // 🔥 Registrar todos los caches explícitamente
        cacheManager.setCacheNames(Arrays.asList(
                "serviciosPorId",
                "serviciosPorUsuario",
                "serviciosPorOficio",
                "todosLosServicios",
                "usuarios",
                "usuariosPorId",
                "todosLosUsuarios",
                "existeUsuario",
                "oficios",
                "oficiosPorId",
                "todosLosOficios",
                "portafoliosPorServicio"
        ));

        return cacheManager;
    }
}