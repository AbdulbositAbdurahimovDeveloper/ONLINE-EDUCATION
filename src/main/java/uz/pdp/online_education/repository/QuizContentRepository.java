package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.lesson.QuizContent;

public interface QuizContentRepository extends JpaRepository<QuizContent, Long> {
}