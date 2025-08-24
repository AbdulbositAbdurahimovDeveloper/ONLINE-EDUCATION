package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.course.CourseStudentStatsProjection;

import java.util.Optional;
import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

    @Query("SELECT DISTINCT c FROM courses c " +
            "JOIN c.modules m " +
            "JOIN m.enrollments e " +
            "WHERE e.user.id = :userId")
    Page<Course> findDistinctEnrolledCoursesForUser(@Param("userId") Long userId, Pageable pageable);

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

    boolean existsByTitle(String title);

    // Barcha mavjud title'larni olish uchun samarali so'rov
    @Query("SELECT c.title FROM courses c")
    Set<String> findAllTitles();

    // Slug'lar ham unique bo'lishi kerakligi uchun
    @Query("SELECT c.slug FROM courses c")
    Set<String> findAllSlugs();

    boolean existsByThumbnailUrl_Id(Long thumbnailUrlId);

    @Query("SELECT c FROM courses c LEFT JOIN FETCH c.modules WHERE c.id = :courseId")
    Optional<Course> findByIdWithModules(Long courseId);

    @Query("SELECT c FROM courses c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Course> searchByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ma'lum bir instructorning 'success' maydoniga qarab kurslari sonini sanaydi.
     *
     * @param instructorId Instructorning IDsi.
     * @param isSuccess    Kursning holati (true=aktiv, false=noaktiv/tasdiqlanmagan).
     * @return Kurslar soni.
     */
    @Query(value = "SELECT COUNT(*) FROM courses c WHERE c.instructor_id = :instructorId AND c.success = :isSuccess and c.deleted = false",
            nativeQuery = true)
    int countByInstructorIdAndSuccess(@Param("instructorId") Long instructorId,
                                      @Param("isSuccess") boolean isSuccess);


    @Query("""
                SELECT COUNT(DISTINCT p.user.id)
                FROM payment p
                WHERE p.module.course.id = :courseId
                  AND p.status = uz.pdp.online_education.enums.TransactionStatus.SUCCESS
            """)
    int countPurchasedUsers(@Param("courseId") Long courseId);

    @Query("""
                SELECT COUNT(DISTINCT me.user.id)
                FROM module_enrollments me
                WHERE me.module.course.id = :courseId
            """)
    int countSubscribedUsers(@Param("courseId") Long courseId);

    Page<Course> findAllByInstructorIdAndSuccess(Long instructorId, boolean success, Pageable pageable);

    @Query(
            value = "SELECT * FROM courses c " +
                    "WHERE c.instructor_id = :instructorId " +
                    "AND c.success = :success " +
                    "AND c.deleted = false " +
                    "ORDER BY c.created_at ASC",
            countQuery = "SELECT COUNT(*) FROM courses c " +
                    "WHERE c.instructor_id = :instructorId " +
                    "AND c.success = :success " +
                    "AND c.deleted = :true " +
                    "AND c.deleted = false",
            nativeQuery = true
    )
    Page<Course> findAllByInstructorIdAndSuccessNative(
            @Param("instructorId") Long instructorId,
            @Param("success") boolean success,
            Pageable pageable
    );

    /**
     * Berilgan qidiruv matni bo'yicha kurslarni sarlavhasi (title)
     * orqali (case-insensitive) qidiradi.
     *
     * @param searchTerm Qidiruv uchun matn
     * @param pageable   Sahifalash uchun ma'lumot
     * @return Topilgan kurslar sahifasi
     */
    @Query("SELECT c FROM courses c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND c.deleted = false")
    Page<Course> searchActiveCoursesByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Course> findAllByInstructorIdAndDeletedFalse(Long instructorId, Pageable pageable);


    @Query("SELECT c FROM courses c JOIN FETCH c.category JOIN FETCH c.instructor i JOIN FETCH i.profile")
    Page<Course> findAllWithDetails(Pageable pageable);

    // Qidiruvni ham xuddi shunday qilamiz
    @Query("SELECT c FROM courses c JOIN FETCH c.category JOIN FETCH c.instructor i JOIN FETCH i.profile WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND c.deleted = false")
    Page<Course> searchActiveCoursesByTitleWithDetails(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Mentor bo'yicha qidiruvni ham xuddi shunday qilamiz
    @Query("SELECT c FROM courses c JOIN FETCH c.category JOIN FETCH c.instructor i JOIN FETCH i.profile WHERE c.instructor.id = :instructorId AND c.deleted = false")
    Page<Course> findAllByInstructorIdWithDetails(@Param("instructorId") Long instructorId, Pageable pageable);


    Page<Course> findAllByCategoryIdAndDeletedFalse(Long categoryId, Pageable pageable);


    @Query(
            value = """
                        SELECT
                            c.id AS course_id,
                            c.title AS course_title,
                            COUNT(DISTINCT p.user_id) AS unique_student_count,
                            COUNT(p.id) AS total_sales_count
                        FROM
                            courses c
                        LEFT JOIN
                            modules m ON c.id = m.course_id
                        LEFT JOIN
                            payment p ON m.id = p.module_id AND p.status = :status
                        WHERE
                            c.instructor_id = :mentorId
                        GROUP BY
                            c.id, c.title
                    """,
            countQuery = """
                        SELECT COUNT(*) FROM (
                            SELECT c.id
                            FROM courses c
                            WHERE c.instructor_id = :mentorId
                            GROUP BY c.id
                        ) AS course_count
                    """,
            nativeQuery = true
    )
    Page<CourseStudentStatsProjection> findCourseStatsByInstructor(
            @Param("mentorId") Long mentorId,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * Berilgan bitta kurs uchun unikal o'quvchilar va jami sotuvlar sonini hisoblaydi.
     * Nativ SQL so'rovidan foydalanadi.
     *
     * @param courseId  Statistikasi olinishi kerak bo'lgan kursning ID'si.
     * @param status    Hisobga olinadigan to'lov statusi (masalan, "SUCCESS").
     * @return Kurs statistikasi bilan to'ldirilgan Projection interfeysi. Agar kurs topilmasa
     *         yoki sotuvlar bo'lmasa, sonlar 0 bo'lib qaytishi mumkin.
     */
    @Query(
            value = """
                        SELECT
                            c.id AS course_id,
                            c.title AS course_title,
                            COUNT(DISTINCT p.user_id) AS unique_student_count,
                            COUNT(p.id) AS total_sales_count
                        FROM
                            courses c
                        LEFT JOIN
                            modules m ON c.id = m.course_id
                        LEFT JOIN
                            payment p ON m.id = p.module_id AND p.status = :status
                        WHERE
                            c.id = :courseId
                        GROUP BY
                            c.id, c.title
                    """,
            nativeQuery = true
    )
    CourseStudentStatsProjection findCourseStatsById(
            @Param("courseId") Long courseId,
            @Param("status") String status
    );
}