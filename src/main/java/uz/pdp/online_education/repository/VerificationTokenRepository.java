package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.List;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
  VerificationToken findByToken(String token);

  List<VerificationToken> findAllByExpiryDateBefore(LocalDateTime now);

}