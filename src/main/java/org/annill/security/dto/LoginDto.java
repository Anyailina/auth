package org.annill.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для аутентификации пользователя.
 * Содержит учетные данные пользователя: имя и пароль.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    private String username;
    private String password;

}
