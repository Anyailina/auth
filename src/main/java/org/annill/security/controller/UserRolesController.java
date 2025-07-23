package org.annill.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.annill.security.dto.RoleDto;
import org.annill.security.dto.SaveRolesDto;
import org.annill.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для управления ролями пользователей.
 * Предоставляет endpoints для получения и изменения ролей пользователей.
 */
@RestController
@RequestMapping("ui/user-roles")
@RequiredArgsConstructor
public class UserRolesController {

    private final UserService userService;

    /**
     * Получает список ролей для указанного пользователя.
     *
     * @param login логин пользователя, для которого запрашиваются роли
     * @param authentication объект аутентификации текущего пользователя
     * @return ResponseEntity со списком DTO ролей пользователя
     */
    @GetMapping("/{login}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Получение ролей для пользователя")
    public ResponseEntity<List<RoleDto>> getRoles(@PathVariable String login, Authentication authentication) {
        return ResponseEntity.ok(userService.getUserRoles(login, authentication));
    }

    /**
     * Сохраняет/обновляет роли для указанного пользователя.
     * Требует прав администратора.
     *
     * @param saveRolesRequest запрос на сохранение ролей, содержащий логин пользователя и список ролей
     * @return ResponseEntity с HTTP статусом 200 (OK) в случае успешного сохранения
     */
    @PutMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Сохранение ролей пользователю")
    public ResponseEntity<?> saveRoles(@RequestBody SaveRolesDto saveRolesRequest) {
        userService.addUserRoles(saveRolesRequest);
        return ResponseEntity.ok().build();
    }

}
