package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    long countByRole(Role role);
}