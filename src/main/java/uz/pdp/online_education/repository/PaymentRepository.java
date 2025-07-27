package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByModule_Id(Long moduleId);

    Page<Payment> findByModule_Id(Long moduleId, Pageable pageable);
}