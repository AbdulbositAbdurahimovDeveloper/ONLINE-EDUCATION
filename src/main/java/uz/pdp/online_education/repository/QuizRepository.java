package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.quiz.Quiz;

import java.util.Optional;
import java.util.Set;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q.title FROM Quiz q")
    Set<String> findAllTitles();

    // Bu metod bitta so'rovda Quiz, uning QuizContent'i, Lesson'i va Module'ini
    // samarali yuklab oladi (N+1 muammosini oldini olish uchun)
    @Query("SELECT q FROM Quiz q " +
            "JOIN FETCH q.quizContent qc " +
            "JOIN FETCH qc.lesson l " +
            "JOIN FETCH l.module " +
            "WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithModule(Long quizId);

}