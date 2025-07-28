package org.annill.security.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с пользователями. Реализует UserDetailsService для интеграции с Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final RoleService roleService;

    /**
     * Находит пользователя по имени.
     *
     * @param username имя пользователя
     * @return сущность пользователя
     */
    public User findByUserName(String username) {
        return userRepository.findByUsername(username).orElseThrow(EntityNotFoundException::new);
    }

    public Optional<User> findByEmailAndTypeRegistration(String email, TypeRegistration typeRegistration) {
        return userRepository.findByEmailAndTypeRegistration(email, typeRegistration);
    }

    /**
     * Проверяет существование пользователя по имени.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    public Boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Проверяет существование пользователя по email.
     *
     * @param username email пользователя
     * @return true, если пользователь существует
     */
    public Boolean existsByEmail(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param signUpDto DTO с данными для регистрации
     */
    public void registerUser(SignUpDto signUpDto) {
        if (existsByUsername(signUpDto.getUsername()) || existsByEmail(signUpDto.getEmail())) {
            throw new EntityExistsException();
        }

        User user = new User(signUpDto.getUsername(), signUpDto.getEmail(), encoder.encode(signUpDto.getPassword()), TypeRegistration.OAUTH);

        Role role = roleService.findByName(ERole.USER).orElseThrow(EntityNotFoundException::new);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    /**
     * Регистрирует нового пользователя с ouath2.
     *
     * @param authentication аутентификация
     */
    public UserDto registerOAuth2(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof OAuth2User oauth2User) {

                String email = oauth2User.getAttribute("email");
                Optional<User> user = findByEmailAndTypeRegistration(email, TypeRegistration.OAUTH);
                String name = oauth2User.getAttribute("name");

                if (user.isEmpty()) {

                    User newUser = new User().setUsername(name).setEmail(email).setTypeRegistration(TypeRegistration.OAUTH2);
                    Role role = roleService.findByName(ERole.USER).orElseThrow(EntityNotFoundException::new);
                    newUser.setRoles(Set.of(role));
                    return UserDto.fromEntity(userRepository.save(newUser));
                } else {
                    throw new EntityExistsException("Пользователь уже существует");
                }

            }


        }
        return null;
    }

    /**
     * Добавляет роли пользователю.
     *
     * @param saveRolesDto запрос на сохранение ролей
     */
    public void addUserRoles(SaveRolesDto saveRolesDto) {
        User user = findByUserName(saveRolesDto.getLogin());
        Collection<Role> roles = user.getRoles();

        Set<Role> newRoles = saveRolesDto.getRoles().stream().map(roleService::findByName).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());

        roles.addAll(newRoles);

        user.setRoles(roles);
        userRepository.save(user);
    }

    /**
     * Получает роли пользователя.
     *
     * @param login          логин пользователя
     * @param authentication данные аутентификации
     * @return список DTO ролей
     */
    public List<RoleDto> getUserRoles(String login, Authentication authentication) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !login.equals(currentUsername)) {
            throw new IllegalAccessError("Доступ разрешен только к своим ролям или для ADMIN");
        }
        User user = findByUserName(login);
        return user.getRoles().stream().map(RoleDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.annill.security.entity.User user = userRepository
                .findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException(String.format("Пользователь 's' не найден", username)));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName().toString())).collect(Collectors.toSet()));
    }

    /**
     * Проверяет учетные данные пользователя.
     *
     * @param loginRequest DTO с учетными данными
     * @return DTO пользователя
     */
    public UserDto getUserAndValidate(LoginDto loginRequest) {
        User user = userRepository.findByUsername(
                loginRequest.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException(String.format("Пользователь 's' не найден", loginRequest.getUsername())));
//        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
//            throw new IllegalAccessError();
//        }
        return UserDto.fromEntity(user);
    }

}
