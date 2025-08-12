package uz.pdp.online_education.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.AdminDashboardDTO;
import uz.pdp.online_education.payload.UserInfo;

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
}