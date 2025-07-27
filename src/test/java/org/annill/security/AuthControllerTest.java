package org.annill.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.annill.security.controller.AuthController;
import org.annill.security.dto.LoginDto;
import org.annill.security.dto.SignUpDto;
import org.annill.security.dto.UserDto;
import org.annill.security.security.JwtUtils;
import org.annill.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtToken() throws Exception {
        LoginDto loginRequest = new LoginDto("test@example.com", "password");
        UserDto userDto =  UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .username( "Test User")
                .roles( new ArrayList<>()).build();
        String expectedToken = "test";

        Mockito.when(userService.getUserAndValidate(loginRequest))
                .thenReturn(userDto);
        Mockito.when(jwtUtils.generateJwtToken(userDto))
                .thenReturn(expectedToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedToken));

        Mockito.verify(userService).getUserAndValidate(loginRequest);
        Mockito.verify(jwtUtils).generateJwtToken(userDto);
    }

    @Test
    void registerUser_ValidRequest_ReturnsOk() throws Exception {
        SignUpDto signUpRequest = new SignUpDto(
                "Test User",
                "test@example.com",
                "password"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userService).registerUser(signUpRequest);
    }

    @Test
    void authenticateUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginDto invalidRequest = new LoginDto("wrong@example.com", "badpassword");

        Mockito.when(userService.getUserAndValidate(invalidRequest))
                .thenThrow(new SecurityException("Invalid credentials"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}