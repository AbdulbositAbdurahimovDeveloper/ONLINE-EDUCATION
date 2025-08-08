package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.quiz.Quiz;

import java.util.Set;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q.title FROM Quiz q")
    Set<String> findAllTitles();

}