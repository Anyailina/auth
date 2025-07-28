package org.annill.security;

import org.annill.security.dto.LoginDto;
import org.annill.security.dto.RoleDto;
import org.annill.security.dto.SaveRolesDto;
import org.annill.security.dto.SignUpDto;
import org.annill.security.dto.UserDto;
import org.annill.security.entity.Role;
import org.annill.security.entity.TypeRegistration;
import org.annill.security.entity.User;
import org.annill.security.repository.UserRepository;
import org.annill.security.roles.ERole;
import org.annill.security.service.RoleService;
import org.annill.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void findByUserName_ShouldReturnUser() {
        User user = new User()
                .setUsername("test")
                .setEmail("test@example.com")
                .setPassword("password")
                .setTypeRegistration(TypeRegistration.OAUTH);
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        User result = userService.findByUserName("test");

        assertEquals(user, result);
    }

    @Test
    void findByUserName_ShouldThrowExceptionWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByUserName("unknown"));
    }

    @Test
    void existsByUsername_ShouldReturnTrue() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(new User()));

        assertTrue(userService.existsByUsername("test"));
    }

    @Test
    void existsByEmail_ShouldReturnTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void registerUser_ShouldSaveNewUser() {
        SignUpDto signUpDto = new SignUpDto("test", "test@example.com", "password");
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("encodedPassword");

        Role userRole = new Role(1, ERole.USER);
        when(roleService.findByName(ERole.USER)).thenReturn(Optional.of(userRole));

        userService.registerUser(signUpDto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowExceptionWhenUsernameExists() {
        SignUpDto signUpDto = new SignUpDto("existing", "test@example.com", "password");
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(EntityExistsException.class, () -> userService.registerUser(signUpDto));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getUserRoles_UserShouldGetOwnRoles() {

        User currentUser = new User()
                .setUsername("test")
                .setEmail("test@example.com")
                .setPassword("password")
                .setTypeRegistration(TypeRegistration.OAUTH);
        Role userRole = new Role(1, ERole.USER);
        currentUser.setRoles(Set.of(userRole));

        when(authentication.getName()).thenReturn("current");
        when(userRepository.findByUsername("current")).thenReturn(Optional.of(currentUser));

        List<RoleDto> result = userService.getUserRoles("current", authentication);

        assertEquals(1, result.size());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getUserRoles_ShouldThrowWhenUnauthorized() {
        when(authentication.getName()).thenReturn("user1");

        assertThrows(IllegalAccessError.class,
                () -> userService.getUserRoles("user2", authentication));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails() {
        User user = new User()
                .setUsername("test")
                .setEmail("test@example.com")
                .setPassword("password")
                .setTypeRegistration(TypeRegistration.OAUTH);
        Role adminRole = new Role(1, ERole.ADMIN);
        user.setRoles(Set.of(adminRole));

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test");

        assertEquals("test", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void getUserAndValidate_ShouldReturnUserDto() {
        String encodedPassword = "encodedPassword";

        LoginDto loginDto = new LoginDto("test", "password");
        User user = new User()
                .setUsername("test")
                .setEmail("test@example.com")
                .setPassword(encodedPassword)
                .setTypeRegistration(TypeRegistration.OAUTH);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        when(encoder.matches("password", encodedPassword)).thenReturn(true);

        UserDto result = userService.getUserAndValidate(loginDto);

        assertEquals("test", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserAndValidate_ShouldThrowOnWrongPassword() {
        String encodedPassword = "encodedPassword";

        LoginDto loginDto = new LoginDto("test", "wrong");
        User user = new User()
                .setUsername("test")
                .setEmail("test@example.com")
                .setPassword(encodedPassword)
                .setTypeRegistration(TypeRegistration.OAUTH);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", encodedPassword)).thenReturn(false);

        assertThrows(IllegalAccessError.class,
                () -> userService.getUserAndValidate(loginDto));
    }


    @Test
    void getUserAndValidate_ShouldThrowOnUserNotFound() {

        LoginDto loginDto = new LoginDto("unknown", "password");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserAndValidate(loginDto));
    }

    @Test
    void registerOAuth2_WhenNewUser_ShouldCreateUser() {
        String email = "new@example.com";
        String name = "New User";
        Role userRole = new Role().setName(ERole.USER);
        User expectedUser = new User()
                .setUsername(name)
                .setEmail(email)
                .setTypeRegistration(TypeRegistration.OAUTH2)
                .setRoles(Set.of(userRole));

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);
        when(userRepository.findByEmailAndTypeRegistration(email, TypeRegistration.OAUTH))
                .thenReturn(Optional.empty());
        when(roleService.findByName(ERole.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        UserDto result = userService.registerOAuth2(authentication);


        assertNotNull(result);
        assertEquals(name, result.getUsername());
        assertEquals(email, result.getEmail());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(email) &&
                        user.getUsername().equals(name) &&
                        user.getTypeRegistration() == TypeRegistration.OAUTH2
        ));
    }

    @Test
    void registerOAuth2_WhenUserExists_ShouldThrowException() {
        String email = "existing@example.com";
        User existingUser = new User().setEmail(email);

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmailAndTypeRegistration(email, TypeRegistration.OAUTH))
                .thenReturn(Optional.of(existingUser));

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> userService.registerOAuth2(authentication)
        );

        assertEquals("Пользователь уже существует", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerOAuth2_WhenRoleNotFound_ShouldThrowException() {
        String email = "new@example.com";
        String name = "New User";

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);

        when(userRepository.findByEmailAndTypeRegistration(email, TypeRegistration.OAUTH))
                .thenReturn(Optional.empty());

        when(roleService.findByName(ERole.USER)).thenReturn(Optional.empty());


        assertThrows(EntityNotFoundException.class,() ->userService.registerOAuth2(authentication));
        verify(userRepository, never()).save(any());
    }
}