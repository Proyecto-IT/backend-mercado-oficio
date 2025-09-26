package com.proyecto_it.mercado_oficio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Security.TestSecurityConfig;
import com.proyecto_it.mercado_oficio.model.Oficio;
import com.proyecto_it.mercado_oficio.service.OficioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.JWT.JwtTokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)

public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OficioController oficioController;

    @MockBean
    private OficioService oficioService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testCrearOficio() throws Exception {
        String json = """
            {
              "nombre": "Carpintero"
            }
        """;

        mockMvc.perform(post("/api/oficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testListarTodos() throws Exception {
        mockMvc.perform(get("/api/oficios"))
                .andExpect(status().isOk())
    }

    @Test
    void testBuscarPorNombre() throws Exception {
        String nombre = "carp";

        mockMvc.perform(get("/api/oficios/buscar")
                        .param("nombre", nombre))
                .andExpect(status().isOk())
    }

    @Test
    void testActualizarOficio() throws Exception {
        String json = """
        {
          "id": "1",
          "nombre": "Plomero"
        }
    """;

        mockMvc.perform(put("/api/oficios/upd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarOficio() throws Exception {
        String id = "carp";

        mockMvc.perform(delete("/api/oficios/borrar")
                        .param("id", id))
                .andExpect(status().isOk())
    }

}
