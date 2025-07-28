package uz.pdp.online_education.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u.username FROM users u")
    Set<String> findAllUsernames();

    List<User> findAllByRole(Role role);
    boolean existsByUsername(String username);
}