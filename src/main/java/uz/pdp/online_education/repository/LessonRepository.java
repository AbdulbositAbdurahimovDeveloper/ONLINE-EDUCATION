package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.lesson.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}