package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.PageDTO;

import java.util.List;
import java.util.Set;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Berilgan modul ichida kamida bitta bepul dars (`isFree = true`) mavjudligini tekshiradi.
     * Bu 'SELECT COUNT(*)' o'rniga 'SELECT 1 ... LIMIT 1' kabi samaraliroq so'rov yuboradi.
     * @param module Tekshirilishi kerak bo'lgan modul
     * @return Agar kamida bitta bepul dars bo'lsa `true`, aks holda `false` qaytaradi.
     */
    boolean existsByModuleAndIsFreeTrue(Module module);

    @Query("SELECT l FROM Lesson l WHERE l.module.id =: moduleId AND l.id IN :lessonIds")
    List<Lesson> findAllModuleIdAndIdIn(Long moduleId, Set<Long> lessonIds);

    Page<Lesson> findByModule_Id(Long moduleId, Pageable pageable);
    List<Lesson> findAllByModuleId(Long moduleId);

    boolean existsByTitleAndModuleId(String title, Long moduleId);

    boolean existsByTitleAndModuleIdAndIdNot(String title, Long moduleId, Long id);

    @Modifying // Bu metodning ma'lumotlarni o'zgartirishini bildiradi
    @Query("UPDATE Lesson l SET l.orderIndex = l.orderIndex - 1 WHERE l.module.id = :moduleId AND l.orderIndex > :orderIndex")
    void shiftOrderIndexesAfterDelete(Long moduleId, Integer orderIndex);

    Page<Lesson> findAllByModule_Id(Long moduleId, Pageable pageable);

    Page<Lesson> findByModule(Module module, Pageable pageable);

    /**
     * Finds all lessons for a specific module, ordered by their index.
     * @param moduleId The ID of the module.
     * @return A sorted list of lessons.
     */
    List<Lesson> findAllByModuleIdOrderByOrderIndexAsc(Long moduleId);


    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.module.id = :moduleId AND l.isFree = true")
    boolean hasFreeLessons(@Param("moduleId") Long moduleId);

    Page<Lesson> findAllByModuleOrderByOrderIndexAsc(Module module, Pageable pageable);
}