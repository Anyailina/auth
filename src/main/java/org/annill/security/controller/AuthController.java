package org.annill.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.annill.security.dto.LoginDto;
import org.annill.security.dto.SignUpDto;
import org.annill.security.dto.UserDto;
import org.annill.security.security.JwtUtils;
import org.annill.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки запросов аутентификации и регистрации пользователей.
 * Предоставляет endpoints для входа в систему и регистрации новых пользователей.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * Аутентифицирует пользователя в системе.
     *
     * @param loginRequest DTO содержащий учетные данные пользователя (логин и пароль)
     * @return JWT токен в случае успешной аутентификации
     */
    @PostMapping("/signin")
    @Operation(summary = "Вход")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginRequest) {
        log.info("Вход");
        UserDto userAndValidate = userService.getUserAndValidate(loginRequest);
        String jwt = jwtUtils.generateJwtToken(userAndValidate);
        return ResponseEntity.ok(jwt);
    }


    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param signUpRequest DTO содержащий данные для регистрации нового пользователя
     * @return ResponseEntity с HTTP статусом 200 (OK) в случае успешной регистрации
     */
    @PostMapping("/signup")
    @Operation(summary = "Регистрация")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpRequest) {
        log.info("Регистрация");
        userService.registerUser(signUpRequest);
        return ResponseEntity.ok().build();
    }

}
