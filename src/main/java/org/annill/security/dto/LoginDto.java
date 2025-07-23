package org.annill.security.dto;

import lombok.Data;
/**
 * DTO для аутентификации пользователя.
 * Содержит учетные данные пользователя: имя и пароль.
 */

@Data
public class LoginDto {

    private String username;
    private String password;

}
