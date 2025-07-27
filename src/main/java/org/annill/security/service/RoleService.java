package org.annill.security.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.annill.security.entity.Role;
import org.annill.security.repository.RoleRepository;
import org.annill.security.roles.ERole;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }

}
