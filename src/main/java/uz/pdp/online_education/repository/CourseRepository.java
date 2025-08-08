package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.course.CourseWithRatingDTO;

import java.util.List;
import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

    /**
     * Barcha kurslarni o'rtacha reytingi bo'yicha kamayish tartibida sahifalab qaytaradi.
     *
     * @param pageable Sahifa raqami, sahifadagi elementlar soni va saralash ma'lumotlarini o'z ichiga oladi.
     * @return Saralangan kurslarning sahifalangan ro'yxati (Page<Course>).
     */
    @Query(
            value = "SELECT c FROM courses c LEFT JOIN c.reviews r GROUP BY c.id ORDER BY COALESCE(AVG(r.rating), 0.0) DESC",
            countQuery = "SELECT COUNT(c) FROM courses c" // @SQLRestriction (deleted=false) avtomatik hisobga olinadi
    )
    Page<Course> findAllOrderByAverageRatingDesc(Pageable pageable);

    /**
     * Finds all courses belonging to a specific category, ordered by their average rating in descending order.
     * Courses with no reviews are treated as having a rating of 0.0.
     * Soft-deleted entities are automatically excluded by @SQLRestriction(deleted=false).
     *
     * @param categoryId The ID of the category to filter by.
     * @param pageable   The pagination information (page number, size, etc.).
     * @return A paginated list of courses for the given category, sorted by rating.
     */
    @Query(
            value = "SELECT c FROM courses c " +
                    "LEFT JOIN c.reviews r " +
                    "WHERE c.category.id = :categoryId " + // <-- QO'SHILGAN SHART
                    "GROUP BY c.id " +
                    "ORDER BY COALESCE(AVG(r.rating), 0.0) DESC",
            countQuery = "SELECT COUNT(c) FROM courses c WHERE c.category.id = :categoryId" // <-- COUNT SO'ROVIGA HAM QO'SHILDI
    )
    Page<Course> findAllByCategoryIdOrderByAverageRatingDesc(@Param("categoryId") Long categoryId, Pageable pageable);
// CourseRepository.java

    // ====================================================================
    // --- SUBQUERY BILAN YANGLANGAN, YAKUNIY FILTER METODI ---
    // ====================================================================
//    @Query(value = "SELECT new uz.pdp.online_education.payload.course.CourseWithRatingDTO(c, COALESCE(AVG(r.rating), 0.0)) " +
//            "FROM Course c " +
//            "LEFT JOIN c.reviews r " +
//            "LEFT JOIN c.instructor i " +
//            "LEFT JOIN i.profile p " +
//            "WHERE " +
//            "(:search IS NULL OR lower(c.title) LIKE lower(concat('%', :search, '%')) OR lower(c.description) LIKE lower(concat('%', :search, '%'))) " +
//            "AND (COALESCE(:categoryTitles, NULL) IS NULL OR c.category.title IN :categoryTitles) " +
//            "AND (COALESCE(:instructorNames, NULL) IS NULL OR i.username IN :instructorNames OR p.firstName IN :instructorNames OR p.lastName IN :instructorNames) " +
//            "AND (:fromPrice IS NULL OR c.price >= :fromPrice) " +
//            "AND (:toPrice IS NULL OR c.price <= :toPrice) " +
//            "GROUP BY c " + // <-- MUHIM O'ZGARISH: Butun entity bo'yicha guruhlash
//            "HAVING (:review IS NULL OR COALESCE(AVG(r.rating), 0.0) >= :review)"
//            // ORDER BY bu yerdan olib tashlandi, uni Pageable orqali dinamik qo'shamiz
//    )
//    Page<CourseWithRatingDTO> filterCoursesWithRating(
//            @Param("search") String search,
//            @Param("categoryTitles") List<String> categoryTitles,
//            @Param("instructorNames") List<String> instructorNames,
//            @Param("fromPrice") Long fromPrice,
//            @Param("toPrice") Long toPrice,
//            @Param("review") Integer review,
//            Pageable pageable
//    );
//    Page<Course> findAll(Specification<Course> spec, Pageable pageable);

    boolean existsByTitle(String title);

    // Barcha mavjud title'larni olish uchun samarali so'rov
    @Query("SELECT c.title FROM courses c")
    Set<String> findAllTitles();

    // Slug'lar ham unique bo'lishi kerakligi uchun
    @Query("SELECT c.slug FROM courses c")
    Set<String> findAllSlugs();

    boolean existsByThumbnailUrl_Id(Long thumbnailUrlId);
}