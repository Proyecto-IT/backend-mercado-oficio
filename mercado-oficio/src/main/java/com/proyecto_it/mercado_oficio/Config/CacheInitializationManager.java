package com.proyecto_it.mercado_oficio.Config;

import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioCacheProxy;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.ServicioCacheProxy;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioCacheProxy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitializationManager {

    private final OficioCacheProxy oficioCacheProxy;
    private final UsuarioCacheProxy usuarioCacheProxy;
    private final ServicioCacheProxy servicioCacheProxy;

    /**
     * 🔥 CRÍTICO: Inicializar caches en el ORDEN CORRECTO
     *
     * 1. Oficios (no tienen dependencias)
     * 2. Usuarios (no tienen dependencias)
     * 3. Servicios (dependen de Usuarios y Oficios)
     * 4. Portafolios (se cargan automáticamente con Servicios)
     */
    @PostConstruct
    @Order(1) // 🔥 Asegurar que se ejecute PRIMERO
    public void inicializarCachesEnOrden() {
        log.info("🚀 ==================== INICIO DE INICIALIZACIÓN DE CACHES ====================");

        try {
            // 1️⃣ OFICIOS PRIMERO (sin dependencias)
            log.info("1️⃣ Inicializando cache de OFICIOS...");
            oficioCacheProxy.inicializarCache();

            // 2️⃣ USUARIOS SEGUNDO (sin dependencias)
            log.info("2️⃣ Inicializando cache de USUARIOS...");
            usuarioCacheProxy.inicializarCache();

            // Pequeña pausa para asegurar que los caches estén listos
            Thread.sleep(100);

            // 3️⃣ SERVICIOS AL FINAL (dependen de Usuarios y Oficios)
            log.info("3️⃣ Inicializando cache de SERVICIOS (con dependencias)...");
            servicioCacheProxy.inicializarCache();

            log.info("✅ ==================== CACHES INICIALIZADOS EXITOSAMENTE ====================");

        } catch (Exception e) {
            log.error("❌ Error fatal al inicializar caches: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudieron inicializar los caches", e);
        }
    }
}
