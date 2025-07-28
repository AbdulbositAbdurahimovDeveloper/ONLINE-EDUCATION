package uz.pdp.online_education.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.UserProfile;

import java.util.Set;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT up.email FROM user_profiles up")
    Set<String> findAllEmails();
    boolean existsByPhoneNumber(String phoneNumber);
}