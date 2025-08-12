package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.quiz.UserAnswer;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // Bitta urinishga tegishli barcha javoblarni, ularning savollari va
    // tanlangan/to'g'ri javob variantlari bilan birga samarali yuklab oladi.
    @Query("SELECT ua FROM user_answers ua " +
            "LEFT JOIN FETCH ua.question q " +
            "LEFT JOIN FETCH q.options " + // Savolning barcha variantlarini
            "LEFT JOIN FETCH ua.selectedOptions " + // Tanlangan variantlarni
            "WHERE ua.attempt.id = :attemptId ORDER BY q.id ASC")
    List<UserAnswer> findAllWithDetailsByAttemptId(Long attemptId);

}