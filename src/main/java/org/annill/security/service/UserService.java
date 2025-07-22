package org.annill.security.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.annill.security.dto.RoleDto;
import org.annill.security.dto.SignUpDto;
import org.annill.security.dto.UserDto;
import org.annill.security.entity.Role;
import org.annill.security.entity.User;
import org.annill.security.repository.UserRepository;
import org.annill.security.roles.ERole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder encoder;


    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(UserDto::fromEntity)
            .orElseThrow(() -> new UsernameNotFoundException(String.format("Пользователь 's' не найден", username)));
    }

    public Boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public Boolean existsByEmail(String username) {
        return userRepository.findByEmail(username).isPresent();
    }


    public void registerUser(SignUpDto signUpDto) {

        if (existsByUsername(signUpDto.getUsername()) || existsByEmail(signUpDto.getEmail())) {
            throw new EntityExistsException();
        }

        User user = new User(signUpDto.getUsername(),
            signUpDto.getEmail(),
            encoder.encode(signUpDto.getPassword()));

        Set<RoleDto> roles = new HashSet<>();

        RoleDto userRole = roleService.findByName(ERole.USER);
        roles.add(userRole);

        saveUsersRoles(roles, user);
    }


    public void saveUsersRoles(Set<RoleDto> roles, User user) {

        Set<Role> roleSet = roles.stream().map(Role::fromDto).collect(Collectors.toSet());

        user.setRoles(roleSet);
        userRepository.save(user);


    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.annill.security.entity.User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(String.format("Пользователь 's' не найден", username)));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .collect(Collectors.toSet()));
    }

}
