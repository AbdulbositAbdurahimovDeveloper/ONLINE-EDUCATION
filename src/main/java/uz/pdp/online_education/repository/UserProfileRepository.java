package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}