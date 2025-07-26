package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.lesson.Lesson;

import java.util.List;
import java.util.Set;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l WHERE l.module.id =: moduleId AND l.id IN :lessonIds")
    List<Lesson> findAllModuleIdAndIdIn(Long moduleId, Set<Long> lessonIds);
}