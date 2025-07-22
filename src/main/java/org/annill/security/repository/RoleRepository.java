package org.annill.security.repository;

import java.util.Optional;
import org.annill.security.entity.Role;
import org.annill.security.roles.ERole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {

    Optional<Role> findByName(ERole name);

}
