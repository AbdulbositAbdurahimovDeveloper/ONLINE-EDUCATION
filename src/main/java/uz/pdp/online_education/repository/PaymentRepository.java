package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.model.User;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByModule_Id(Long moduleId, Pageable pageable);

    /**
     * Finds all unique users who have made at least one payment.
     * @return A list of unique User entities.
     */
    @Query("SELECT DISTINCT p.user FROM payment p")
    List<User> findAllDistinctUsers();

    /**
     * Finds all courses that a specific user has paid for.
     * @param userId The ID of the user.
     * @return A list of unique Course entities purchased by the user.
     */
    @Query("SELECT DISTINCT p.module.course FROM payment p WHERE p.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);

    boolean existsByUser_UsernameAndModule_Id(String userUsername, Long moduleId);

    /**
     * Checks if a successful payment exists for a specific user and module.
     * @return true if a successful payment is found, otherwise false.
     */
    boolean existsByUserIdAndModuleIdAndStatus(Long userId, Long moduleId, TransactionStatus status);

    boolean existsByUserAndModuleId(User user, Long moduleId);

    boolean existsByUser_IdAndModule_Id(Long id, Long id1);

    // 1-so'rov: Foydalanuvchining umumiy to'lagan summasini olish
    @Query("SELECT SUM(p.amount) FROM payment p WHERE p.user.id = :userId AND p.status = :status")
    Long findTotalSuccessfulPaymentsByUserId(@Param("userId") Long userId, @Param("status") TransactionStatus status);

    // 2-so'rov: Foydalanuvchining oxirgi muvaffaqiyatli to'lovini topish
    Optional<Payment> findTopByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, TransactionStatus status);

    long countByUser_IdAndStatus(Long id, TransactionStatus transactionStatus);

    /**
     * Foydalanuvchining barcha muvaffaqiyatli to'lovlarini (SUCCESS statusli)
     * sahifalangan va saralangan holda topadi.
     *
     * @param user     Foydalanuvchi obyekti
     * @param status   To'lov statusi (SUCCESS bo'lishi kerak)
     * @param pageable Sahifalash va saralash ma'lumotlari.
     *                 Saralash uchun Pageable'da createdAt DESC (yoki shu kabi) belgilanishi kerak.
     * @return To'lovlar sahifasi (Page<Payment>)
     */
    Page<Payment> findByUserAndStatus(User user, TransactionStatus status, Pageable pageable);

    // Boshqa variant: User IDsi bo'yicha qidirish
    Page<Payment> findByUserIdAndStatus(Long userId, TransactionStatus status, Pageable pageable);

}