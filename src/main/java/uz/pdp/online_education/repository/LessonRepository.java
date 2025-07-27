package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.PageDTO;

import java.util.List;
import java.util.Set;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByModuleId(Long moduleId);

    boolean existsByTitleAndModuleId(String title, Long moduleId);

    boolean existsByTitleAndModuleIdAndIdNot(String title, Long moduleId, Long id);

    @Modifying // Bu metodning ma'lumotlarni o'zgartirishini bildiradi
    @Query("UPDATE Lesson l SET l.orderIndex = l.orderIndex - 1 WHERE l.module.id = :moduleId AND l.orderIndex > :orderIndex")
    void shiftOrderIndexesAfterDelete(Long moduleId, Integer orderIndex);

    Page<Lesson> findAllByModule_Id(Long moduleId, Pageable pageable);
}