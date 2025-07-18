package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}