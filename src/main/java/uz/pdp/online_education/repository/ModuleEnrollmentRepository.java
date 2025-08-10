package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.model.ModuleEnrollment;

@Repository
public interface ModuleEnrollmentRepository extends JpaRepository<ModuleEnrollment, Long> {

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
}