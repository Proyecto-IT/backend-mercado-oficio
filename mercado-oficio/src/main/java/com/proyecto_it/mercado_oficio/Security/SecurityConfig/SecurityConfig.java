package com.proyecto_it.mercado_oficio.Security.SecurityConfig;

import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtAuthenticationFilter;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
@Profile("!test") // Solo se activa fuera de tests
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationProvider jwtProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, JwtAuthenticationProvider jwtProvider) {
        this.jwtFilter = jwtFilter;
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ENDPOINTS PÚBLICOS - AUTH
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/validate",
                                "/api/auth/login",
                                "/api/auth/refresh",  // refresh es público porque usa cookie
                                "/api/auth/reset-password-request",
                                "/api/auth/reset-password/confirm",
                                "/api/auth/oauth2/success",
                                "/api/usuario/confirmar-email"
                        ).permitAll()

                        // MERCADOPAGO
                        .requestMatchers(
                                "/api/mp/**",
                                "/pago-exitoso",
                                "/pago-pendiente",
                                "/pago-fallido"
                        ).permitAll()

                        // OAUTH2
                        .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()

                        // ENDPOINTS PROTEGIDOS - AUTH
                        .requestMatchers("/api/auth/me").authenticated() // ← CRÍTICO: debe estar autenticado
                        .requestMatchers(HttpMethod.PUT, "/api/auth/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        // USUARIO
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        // OFICIOS
                        .requestMatchers(HttpMethod.GET, "/api/oficios/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/oficios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/oficios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/oficios/**").hasRole("ADMIN")

                        // RESTO DE API - GET permitido para autenticados
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        // CUALQUIER OTRA PETICIÓN
                        .anyRequest().authenticated()
                )
                // Autenticación
                .authenticationProvider(daoAuthenticationProvider)
                .authenticationProvider(jwtProvider)
                // JWT filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Deshabilitar login clásico
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://www.mercadopago.com.ar",
                "https://www.mercadopago.com"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie")); // ← Exponer headers necesarios

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}