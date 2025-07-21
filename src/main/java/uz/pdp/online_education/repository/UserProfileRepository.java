package uz.pdp.online_education.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}