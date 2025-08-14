package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Berilgan kursga tegishli barcha asosiy kommentlarni (parent_id=NULL) topadi.
    List<Comment> findAllByCourseIdAndParentIsNull(Long courseId);

    // Berilgan darsga tegishli barcha asosiy kommentlarni (parent_id=NULL) topadi.
    List<Comment> findAllByLessonIdAndParentIsNull(Long lessonId);

    // Barcha asosiy kommentlarni (parent_id=NULL) topadi.
    List<Comment> findAllByParentIsNull();
}