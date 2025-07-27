package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // SIZNING MAVJUD METODINGIZ
    Page<Payment> findByModule_Id(Long moduleId, Pageable pageable);

    // --- QO'SHILADIGAN YANGI METODLAR ---

    /**
     * Foydalanuvchi va modul ID si bo'yicha muvaffaqiyatli to'lov mavjudligini tekshiradi.
     * @param userId Foydalanuvchi ID si.
     * @param moduleId Modul ID si.
     * @param status Tranzaksiya holati (masalan, SUCCESS).
     * @return To'lov mavjud bo'lsa true, aks holda false qaytaradi.
     */
    boolean existsByUserIdAndModuleIdAndStatus(Long userId, Long moduleId, TransactionStatus status);

    /**
     * Foydalanuvchi berilgan kursdagi nechta modulni sotib olganini sanaydi.
     * @param userId Foydalanuvchi ID si.
     * @param courseId Kurs ID si.
     * @return Sotib olingan modullar soni.
     */
    @Query("SELECT COUNT(DISTINCT p.module.id) FROM Payment p WHERE p.user.id = :userId AND p.module.course.id = :courseId AND p.status = 'SUCCESS'")
    long countPurchasedModulesInCourse(Long userId, Long courseId);
}