package org.annill.security.repository;

import java.util.Optional;

import org.annill.security.entity.TypeRegistration;
import org.annill.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailAndTypeRegistration(String email, TypeRegistration typeRegistration);

    Optional<User> findByEmail(String email);

}
