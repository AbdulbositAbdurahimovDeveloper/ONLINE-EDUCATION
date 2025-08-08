package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;

import java.util.List;
import java.util.Set;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Page<Module> findByCourseId(Long courseId, Pageable pageable);

    List<Module> findAllByCourse_Id(Long courseId);

    Long id(Long id);

    /**
     * Decrements the orderIndex of all modules in a given course that have an orderIndex
     * greater than the specified index. This is used for reordering after a deletion.
     *
     * @param course            The course whose modules are to be reordered.
     * @param deletedOrderIndex The order index of the module that was deleted.
     */
    @Modifying // Bu metod ma'lumotlarni o'zgartirishini bildiradi (UPDATE, DELETE)
    @Query("UPDATE modules m SET m.orderIndex = m.orderIndex - 1 " +
            "WHERE m.course = :course AND m.orderIndex > :deletedOrderIndex")
    void shiftOrderIndexesAfterDelete(@Param("course") Course course, @Param("deletedOrderIndex") Integer deletedOrderIndex);

    @Query("SELECT m.title FROM modules m")
    Set<String> findAllTitles();

    boolean existsByTitle(String title);

    long countById(Long id);
}