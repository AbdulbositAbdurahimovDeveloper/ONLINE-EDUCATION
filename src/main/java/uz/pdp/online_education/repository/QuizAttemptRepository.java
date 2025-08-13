package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.enums.AttemptStatus;
import uz.pdp.online_education.model.quiz.QuizAttempt;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByUserIdAndQuizIdAndStatus(Long userId, Long quizId, AttemptStatus status);
}