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
@Profile("!test")
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
                                "/api/auth/refresh",
                                "/api/auth/reset-password-request",
                                "/api/auth/reset-password/confirm",
                                "/api/auth/oauth2/success",
                                "/api/usuario/confirmar-email"
                        ).permitAll()
                        // OAUTH2
                        .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                        // ENDPOINTS PROTEGIDOS - AUTH
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        //USUARIO
                        .requestMatchers(HttpMethod.GET, "/api/usuario/me/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/me/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/usuario/me/**").authenticated()

                        // USUARIO - RUTAS GENERALES (otros usuarios) CON ROLES
                        .requestMatchers(HttpMethod.GET, "/api/usuario/{gmail}").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/{gmail}").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        // OFICIOS
                        .requestMatchers(HttpMethod.GET, "/api/oficios/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/oficios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/oficios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/oficios/**").hasRole("ADMIN")

                        // SERVICIOS
                        .requestMatchers(HttpMethod.GET, "/api/servicios/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios/usuario/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios/oficio/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/servicios").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasRole("TRABAJADOR")

                        //PRESUPUESTO
                        .requestMatchers(HttpMethod.GET, "/api/presupuestos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.POST, "/api/presupuestos").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/presupuestos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/presupuestos/*/estado").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/presupuestos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        // HITOS
                        .requestMatchers(HttpMethod.POST, "/api/hitos/crear").hasAnyRole("CLIENTE", "TRABAJADOR")  // ← Específica primero
                        .requestMatchers(HttpMethod.GET, "/api/hitos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.POST, "/api/hitos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/hitos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/hitos/**").hasAnyRole("CLIENTE", "TRABAJADOR")
                        // RESTO DE API - GET para autenticados
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CLIENTE", "ADMIN", "TRABAJADOR")

                        // CUALQUIER OTRA PETICIÓN
                        .anyRequest().authenticated()
                )
                .authenticationProvider(daoAuthenticationProvider)
                .authenticationProvider(jwtProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
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
                "https://www.mercadopago.com",
                "https://mercado-oficio.netlify.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

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