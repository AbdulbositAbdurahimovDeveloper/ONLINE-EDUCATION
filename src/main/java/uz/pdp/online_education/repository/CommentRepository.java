package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    List<Comment> findAllByCourseIdAndParentIsNull(Long courseId);


    List<Comment> findAllByLessonIdAndParentIsNull(Long lessonId);


    List<Comment> findAllByParentIsNull();
}