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

import java.math.BigDecimal;
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



    /**
     * Ma'lum bir instructorning barcha kurslari bo'yicha umumiy daromadini hisoblaydi.
     * Faqat muvaffaqiyatli to'lovlar hisobga olinadi. Natija tiyinlarda (Long) qaytariladi.
     *
     * @param instructorId Instructorning IDsi.
     * @return Umumiy daromad (Long), agar daromad bo'lmasa 0 qaytaradi.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0L) " + // 0 o'rniga 0L ishlatamiz, bu Long ekanligini bildiradi
            "FROM payment p " +
            "WHERE p.module.course.instructor.id = :instructorId AND p.status = 'SUCCESS'")
    Long calculateTotalIncomeByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Ma'lum bir instructorning kurslariga tegishli modullarni sotib olgan
     * unikal foydalanuvchilar sonini sanaydi. Faqat muvaffaqiyatli to'lovlar
     * hisobga olinadi.
     *
     * @param instructorId Instructorning IDsi.
     * @return Unikal o'quvchilar soni.
     */
    @Query("SELECT COUNT(*) " +
            "FROM payment p " +
            "WHERE p.module.course.instructor.id = :instructorId AND p.status = 'SUCCESS'")
    Integer countDistinctPurchasedUsersByInstructorId(@Param("instructorId") Long instructorId);



    long countByModule_Course_Id(Long id);

    boolean existsByModule_Course_Id(Long id);

    long countByModule_Id(Long id);

    /**
     * Berilgan mentorning barcha kurslari bo'yicha unikal o'quvchilar sonini
     * JPQL so'rovi yordamida hisoblaydi. JOIN'lar aniq ko'rsatilgan.
     *
     * @param mentorId Mentor (instruktor) ID'si.
     * @param status   Hisobga olinadigan to'lov statusi.
     * @return Unikal o'quvchilar soni.
     */
    @Query("""
                SELECT COUNT(DISTINCT p.user.id)
                FROM payment p
                JOIN p.module m
                JOIN m.course c
                WHERE c.instructor.id = :mentorId
                  AND p.status = :status
            """)
    Long countTotalStudentsByMentor(
            @Param("mentorId") Long mentorId,
            @Param("status") TransactionStatus status
    );
}