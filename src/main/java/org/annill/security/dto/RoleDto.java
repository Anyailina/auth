package org.annill.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.annill.security.entity.Role;
import org.annill.security.roles.ERole;

/**
 * DTO для представления роли.
 * Содержит идентификатор роли и её название.
 * Поддерживает преобразование из сущности Role.
 */

@Value
@Builder(toBuilder = true)
@Schema
public class RoleDto {

    private Integer id;

    private ERole name;

    public static RoleDto fromEntity(Role role) {
        return RoleDto.builder().id(role.getId()).name(role.getName()).build();
    }

}
