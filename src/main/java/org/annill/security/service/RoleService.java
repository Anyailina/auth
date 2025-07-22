package org.annill.security.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.annill.security.dto.RoleDto;
import org.annill.security.repository.RoleRepository;
import org.annill.security.roles.ERole;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleDto findByName(ERole name) {
        return roleRepository.findByName(name).map(RoleDto::fromEntity)
            .orElseThrow(EntityNotFoundException::new);
    }

}
