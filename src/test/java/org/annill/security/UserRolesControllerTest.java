package org.annill.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.annill.security.controller.UserRolesController;
import org.annill.security.dto.RoleDto;
import org.annill.security.dto.SaveRolesDto;
import org.annill.security.roles.ERole;
import org.annill.security.security.JwtUtils;
import org.annill.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRolesController.class)
class UserRolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"USER"})
    void getRoles_ShouldReturnRoles() throws Exception {
        String login = "test@example.com";
        RoleDto user = RoleDto.builder().id(1).name(ERole.USER).build();
        RoleDto admin = RoleDto.builder().id(2).name(ERole.ADMIN).build();
        List<RoleDto> roles = Arrays.asList(
                user, admin
        );

        when(userService.getUserRoles(eq(login), any(Authentication.class)))
                .thenReturn(roles);

        mockMvc.perform(get("/ui/user-roles/{login}", login))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("USER"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void saveRoles_ShouldSaveSuccessfully() throws Exception {
        SaveRolesDto request = new SaveRolesDto(
                "test@example.com",
                Arrays.asList(ERole.ADMIN, ERole.USER)
        );

        doNothing().when(userService).addUserRoles(any(SaveRolesDto.class));

        mockMvc.perform(put("/ui/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // Добавляем CSRF токен
                .andExpect(status().isOk());

        verify(userService, times(1)).addUserRoles(any(SaveRolesDto.class));
    }

    @Test
    void saveRoles_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        SaveRolesDto request = new SaveRolesDto(
                "test@example.com",
                List.of(ERole.USER)
        );

        mockMvc.perform(put("/ui/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(userService, never()).addUserRoles(any());
    }

    @Test
    void getRoles_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/ui/user-roles/test@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }
}