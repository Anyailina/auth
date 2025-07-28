package org.annill.security.dto;

import java.util.Collection;

import lombok.Builder;
import lombok.Value;
import org.annill.security.entity.Role;
import org.annill.security.entity.TypeRegistration;
import org.annill.security.entity.User;

/**
 * DTO для представления пользователя.
 * Содержит основные данные пользователя и список его ролей.
 * Поддерживает преобразование из сущности User.
 */
@Value
@Builder(toBuilder = true)
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private TypeRegistration typeRegistration;

    private Collection<Role> roles;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .typeRegistration(user.getTypeRegistration())
                .build();
    }

}
