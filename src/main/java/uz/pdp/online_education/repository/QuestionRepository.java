package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.model.quiz.Quiz;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Collection<Object> findAllByQuizId(Long quizId);
}