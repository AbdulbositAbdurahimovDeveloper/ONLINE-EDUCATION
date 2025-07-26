package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}