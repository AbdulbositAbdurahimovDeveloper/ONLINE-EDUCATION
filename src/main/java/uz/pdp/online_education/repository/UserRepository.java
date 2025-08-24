package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.AdminDashboardDTO;
import uz.pdp.online_education.payload.UserInfo;
import uz.pdp.online_education.payload.user.UserProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u.username FROM users u")
    Set<String> findAllUsernames();

    List<User> findAllByRole(Role role);

    boolean existsByUsername(String username);

    @Query(value = """
                SELECT
                    (SELECT COUNT(*) FROM users u WHERE u.deleted = false) AS totalUsers,
            
                    (SELECT COUNT(*) FROM users i WHERE i.role = 'INSTRUCTOR' AND i.deleted = false) AS totalInstructors,
            
                    (SELECT COUNT(*) FROM courses c WHERE c.deleted = false) AS totalCourses,
            
                    (SELECT COALESCE(SUM(p.amount), 0) FROM payment p
                     WHERE p.status = 'SUCCESS' AND DATE_TRUNC('month', p.created_at) = DATE_TRUNC('month', CURRENT_DATE)) AS revenueThisMonth,
            
                    (SELECT COUNT(*) FROM users u_today WHERE u_today.deleted = false AND DATE(u_today.created_at) = CURRENT_DATE) AS newUsersToday,
            
                    (SELECT COUNT(*) FROM payment p_today
                     WHERE p_today.status = 'SUCCESS' AND DATE(p_today.created_at) = CURRENT_DATE) AS salesToday,
            
                    -- YORDAM SO'ROVLARI UCHUN SO'ROV (Tuzatildi)
                    (SELECT COUNT(*) FROM contact_messages cm
                     WHERE cm.status = 'NEW') AS newSupportTickets
            """, nativeQuery = true)
    AdminDashboardDTO getAdminDashboardStats();

    @Query("SELECT u.id as id, up.firstName as firstName, up.lastName as lastName, count(co.id) as courseCount " +
            "FROM users u JOIN u.profile up JOIN courses co ON co.instructor.id = u.id " +
            "WHERE u.role = 'INSTRUCTOR' AND co.deleted = false AND co.success = true " +
            "GROUP BY u.id, up.firstName, up.lastName " +
            "HAVING count(co.id) > 0 " +
            "ORDER BY up.firstName")
    Page<UserInfo> findInstructorsWithCourseCount(Pageable pageable);

    /**
     * Kamida bitta tasdiqlangan (success=true) kursi bor bo'lgan instruktorlarni,
     * ularning tasdiqlangan kurslari soni bilan birga, sahifalangan holda qaytaradi.
     *
     * @param pageable Sahifalash va saralash ma'lumotlari
     * @return UserInfo DTO'laridan iborat Page obyekti
     */
    @Query("SELECT u.id as id, up.firstName as firstName, up.lastName as lastName, count(co.id) as courseCount " +
            "FROM users u JOIN u.profile up JOIN u.courses co " +
            "WHERE u.role = 'INSTRUCTOR' AND co.success = true " +
            "GROUP BY u.id, up.firstName, up.lastName " +
            "ORDER BY count(co.id) DESC, up.firstName ASC")
    Page<UserInfo> findInstructorsWithSuccessfulCourses(Pageable pageable);


    /**
     * Berilgan qidiruv matni bo'yicha foydalanuvchilarni qidiradi.
     * Qidiruv username, email, ism va familiya bo'yicha (case-insensitive) amalga oshiriladi.
     *
     * @param searchTerm Qidiruv uchun matn
     * @param pageable   Sahifalash uchun ma'lumot
     * @return Topilgan foydalanuvchilar sahifasi
     */
    @Query("""
                SELECT u FROM users u JOIN u.profile p WHERE
                LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            """)
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<User> findAllByRole(Role role, Pageable pageable);

    @Query(
            value = """
                        SELECT DISTINCT
                            u.id AS id,
                            u.username AS username,
                            u.role AS role,
                            up.first_name AS firstName,
                            up.last_name AS lastName,
                            up.email AS email,
                            up.phone_number AS phoneNumber,
                            up.bio AS bio,
                            up.profile_picture_id AS profilePictureId,
                            u.created_at AS createdAt,
                            u.updated_at AS updatedAt,
                            r.rating AS rating
                        FROM
                            users u
                        INNER JOIN
                            payment p ON u.id = p.user_id
                        INNER JOIN
                            modules m ON p.module_id = m.id
                        LEFT JOIN
                            user_profiles up ON u.id = up.user_id
                        LEFT JOIN
                            reviews r ON u.id = r.user_id AND m.course_id = r.course_id
                        WHERE
                            m.course_id = :courseId
                            AND p.status = :status
                    """,
            countQuery = """
                        SELECT COUNT(DISTINCT u.id)
                        FROM users u
                        INNER JOIN payment p ON u.id = p.user_id
                        INNER JOIN modules m ON p.module_id = m.id
                        WHERE m.course_id = :courseId
                          AND p.status = :status
                    """,
            nativeQuery = true
    )
    Page<UserProjection> findEnrolledStudentProfilesByCourseId(
            @Param("courseId") Long courseId,
            @Param("status") String status,
            Pageable pageable
    );
}