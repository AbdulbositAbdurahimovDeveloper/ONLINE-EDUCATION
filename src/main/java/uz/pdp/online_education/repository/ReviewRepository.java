package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Review;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByCourseIdAndUserId(Long courseId, Long userId);

}
