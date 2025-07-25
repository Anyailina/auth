package org.annill.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * DTO для аутентификации пользователя.
 * Содержит учетные данные пользователя: имя и пароль.
 */

@Data
@AllArgsConstructor
public class LoginDto {

    private String username;
    private String password;

}
