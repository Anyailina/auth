package org.annill.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для регистрации нового пользователя.
 * Содержит основные данные пользователя: имя, email и пароль.
 */

@Data
@AllArgsConstructor
public class SignUpDto {

    private String username;
    private String email;
    private String password;

}
