package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Barcha kurslarni o'rtacha reytingi bo'yicha kamayish tartibida sahifalab qaytaradi.
     * @param pageable Sahifa raqami, sahifadagi elementlar soni va saralash ma'lumotlarini o'z ichiga oladi.
     * @return Saralangan kurslarning sahifalangan ro'yxati (Page<Course>).
     */
    @Query(
            value = "SELECT c FROM courses c LEFT JOIN c.reviews r GROUP BY c.id ORDER BY COALESCE(AVG(r.rating), 0.0) DESC",
            countQuery = "SELECT COUNT(c) FROM courses c" // @SQLRestriction (deleted=false) avtomatik hisobga olinadi
    )
    Page<Course> findAllOrderByAverageRatingDesc(Pageable pageable);


}