package org.annill.security.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.annill.security.roles.ERole;

/**
 * DTO для сохранения ролей пользователя.
 * Содержит логин пользователя и список ролей, которые необходимо сохранить.
 */
@Getter
@Setter
@AllArgsConstructor
public class SaveRolesDto {

    private String login;
    private List<ERole> roles;

}
