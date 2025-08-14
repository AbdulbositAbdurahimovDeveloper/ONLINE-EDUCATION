package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.model.quiz.Quiz;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Collection<Object> findAllByQuizId(Long quizId);

    Optional<Question> findFirstByQuizIdOrderByIdAsc(Long quizId);

    /**
     * Berilgan test ichida va berilgan ID'dan katta bo'lgan birinchi savolni topadi.
     * Bu "Keyingi Savol" tugmasi uchun kerak.
     * @param quizId Test ID'si
     * @param currentQuestionId Joriy savolning ID'si
     * @return Topilgan keyingi savol yoki bo'sh Optional
     */
    Optional<Question> findFirstByQuizIdAndIdGreaterThanOrderByIdAsc(Long quizId, Long currentQuestionId);
}
