package org.annill.security.dto;

import lombok.Builder;
import lombok.Value;
import org.annill.security.entity.Role;
import org.annill.security.roles.ERole;

@Value
@Builder(toBuilder = true)
public class RoleDto {

    private Integer id;

    private ERole name;


    public static RoleDto fromEntity(Role role) {
        return RoleDto.builder()
            .id(role.getId())
            .name(role.getName())
            .build();
    }
}
