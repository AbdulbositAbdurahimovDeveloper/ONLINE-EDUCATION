package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Faq;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}
