package com.proyecto_it.mercado_oficio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.JWT.JwtTokenService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthResponse;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.proyecto_it.mercado_oficio.Web.AuthController;
import org.junit.jupiter.api.Test;
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
    private AuthController authController;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    /*@Test
    void testRegisterUsuario() throws Exception {
        String json = """
            {
              "gmail": "thiago2007crackz@gmail.com",
              "nombre": "Thiago",
              "apellido": "Velazquez",
              "password": "Admin123"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuario registrado correctamente")));
    }

    @Test
    void testValidarEmail() throws Exception {
        String token = "98cb19f7-43a1-4f20-8f54-1d2256cbd838";

        mockMvc.perform(get("/api/auth/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuario validado correctamente.")));
    }


    @Test
    void testLoginUsuario() throws Exception {
        String json = """
        {
          "gmail": "thiago2007crackz@gmail.com",
          "password": "Admin123"
        }
    """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testRefreshToken() throws Exception {
        String requestJson = """
        {
            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0NMSUVOVEUiXSwic3ViIjoidGhpYWdvMjAwN2NyYWNrekBnbWFpbC5jb20iLCJpYXQiOjE3NTgzMjA0OTQsImV4cCI6MTc1ODkyNTI5NH0.u-oDjhYnZ8aiYx47JvdCubYuNiK6MfRTcEI89hTs2-I"
        }
    """;



        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.usuarioId").exists());
    }

     */
}
