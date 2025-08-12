package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.payload.CategoryInfo;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);


    @Query("SELECT c.id as id, c.name as name, count(co.id) as courseCount " +
            "FROM category c JOIN c.courses co " +
            "WHERE co.deleted = false AND co.success = true " +
            "GROUP BY c.id, c.name " +
            "HAVING count(co.id) > 0 " +
            "ORDER BY c.name")
    Page<CategoryInfo> findCategoriesWithCourseCount(Pageable pageable);
    boolean existsByName(String name);
}