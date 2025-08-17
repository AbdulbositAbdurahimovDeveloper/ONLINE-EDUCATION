package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
import uz.pdp.online_education.model.User;

import java.util.Collection;
import java.util.List;

@Repository
public interface ModuleEnrollmentRepository extends JpaRepository<ModuleEnrollment, Long> {

    @Query("SELECT me.module.id FROM module_enrollments me WHERE me.user.id = :userId AND me.module.course.id = :courseId")
    List<Long> findEnrolledModuleIdsByUser(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * Counts the number of unique, in-progress (not completed) courses for a specific user.
     * This query joins through Module to get to the Course entity.
     *
     * @param userId The ID of the user.
     * @return The number of active courses. Returns 0 if none.
     */
    @Query("SELECT COUNT(DISTINCT me.module.course) FROM module_enrollments me WHERE me.user.id = :userId AND me.progressPercentage < 100.0")
    Integer countActiveCoursesByUserId(@Param("userId") Long userId);

    /**
     * Calculates the average progress percentage across all enrolled modules for a specific user.
     * If the user has no enrollments, this will return null.
     *
     * @param userId The ID of the user.
     * @return The average progress as a Double, or null if no enrollments.
     */
    @Query("SELECT AVG(me.progressPercentage) FROM module_enrollments me WHERE me.user.id = :userId")
    Double findAverageProgressByUserId(@Param("userId") Long userId);

    /**
     * Counts the number of modules a user has fully completed (progress is 100%).
     * This can be used as a proxy for the number of certificates.
     *
     * @param userId The ID of the user.
     * @return The number of completed modules.
     */
    @Query("SELECT COUNT(me) FROM module_enrollments me WHERE me.user.id = :userId AND me.progressPercentage = 100.0")
    Integer countCompletedModulesByUserId(@Param("userId") Long userId);


    /**
     * Finds a paginated list of all unique courses a user is enrolled in,
     * regardless of the enrollment status or course success status.
     *
     * @param userId The ID of the user whose enrolled courses are to be found.
     * @param pageable Pagination information.
     * @return A paginated list of unique {@link Course} entities.
     */
//    @Query("SELECT DISTINCT me.module.course FROM ModuleEnrollment me WHERE me.user.id = :userId")
//    Page<Course> findEnrolledCoursesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Calculates the average progress percentage for a specific user within a specific course.
     * It does this by averaging the progress of all modules the user is enrolled in for that course.
     *
     * @param userId   The ID of the user.
     * @param courseId The ID of the course.
     * @return The average progress as a Double, or null if no relevant enrollments are found.
     */
    @Query("SELECT AVG(me.progressPercentage) " +
            "FROM module_enrollments me " +
            "WHERE me.user.id = :userId AND me.module.course.id = :courseId")
    Double findAverageProgressForCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * Finds all enrollments for a specific user within a specific course.
     * The results are ordered by the module's orderIndex to display them correctly.
     *
     * @param userId   The ID of the user.
     * @param courseId The ID of the course.
     * @return A list of ModuleEnrollments.
     */
    @Query("SELECT me FROM module_enrollments me " +
            "WHERE me.user.id = :userId AND me.module.course.id = :courseId " +
            "ORDER BY me.module.orderIndex ASC")
    List<ModuleEnrollment> findEnrollmentsByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    Collection<ModuleEnrollment> findAllByUserId(Long userId);

    boolean existsByUserIdAndModuleId(Long userId, Long moduleId);

    Collection<ModuleEnrollment> findAllByModuleId(Long moduleId);

    /**
     * Finds all unique courses a user is enrolled in (regardless of payment status).
     * This is used for the "My Courses" section, which acts as a personal cabinet.
     */
    @Query("SELECT DISTINCT me.module.course FROM module_enrollments me WHERE me.user.id = :userId")
    Page<Course> findEnrolledCoursesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(me) FROM module_enrollments me WHERE me.user.id = :userId AND me.module.course.id = :courseId")
    long countByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    boolean existsByUserAndModuleId(User user, Long moduleId);

    // 3-so'rov: Foydalanuvchining to'lov qilmagan modullarini topish
    @Query("SELECT me.module FROM module_enrollments me WHERE me.user.id = :userId AND me.module.id NOT IN " +
            "(SELECT p.module.id FROM payment p WHERE p.user.id = :userId AND p.status = uz.pdp.online_education.enums.TransactionStatus.SUCCESS)")
    List<Module> findUnpaidModulesByUserId(@Param("userId") Long userId);

    /**
     * Berilgan foydalanuvchi uchun yozilgan, lekin hali muvaffaqiyatli to'lov qilinmagan
     * barcha modullarni sahifalangan va saralangan holda topadi.
     *
     * @param userId   Foydalanuvchi IDsi
     * @param pageable Sahifalash va saralash uchun ma'lumotlar (masalan, PageRequest.of(0, 5, Sort.by("module.title")))
     * @return To'lanmagan modullar sahifasi (Page<Module>)
     */
    @Query("SELECT me.module FROM module_enrollments me WHERE me.user.id = :userId AND me.module.id NOT IN " +
            "(SELECT p.module.id FROM payment p WHERE p.user.id = :userId AND p.status = uz.pdp.online_education.enums.TransactionStatus.SUCCESS)")
    Page<Module> findUnpaidModulesByUserId(@Param("userId") Long userId, Pageable pageable);
}