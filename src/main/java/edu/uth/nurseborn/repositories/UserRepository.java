package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user theo username
    Optional<User> findByUsername(String username);

    // Tìm user theo email
    Optional<User> findByEmail(String email);

    // Tìm user theo role
    List<User> findByRole(Role role);

    // Kiểm tra tồn tại theo username hoặc email
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}