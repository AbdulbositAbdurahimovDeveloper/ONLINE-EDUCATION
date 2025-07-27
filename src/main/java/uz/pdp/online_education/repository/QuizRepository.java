package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.quiz.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}