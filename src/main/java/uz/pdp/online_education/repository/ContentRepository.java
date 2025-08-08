package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.model.lesson.Lesson;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Modifying
    @Query("UPDATE Content c SET c.blockOrder = c.blockOrder - 1 WHERE c.lesson.id = :lessonId AND c.blockOrder > :blockOrder")
    void shiftBlockOrdersAfterDelete(Long lessonId, int blockOrder);

    List<Content> findAllByLessonId(Long lessonId);
}