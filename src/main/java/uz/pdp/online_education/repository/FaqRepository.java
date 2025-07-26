package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uz.pdp.online_education.model.Faq;

import java.util.List;
import java.util.Optional;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    @Query("SELECT COALESCE(MAX(f.displayOrder), -1) FROM Faq f WHERE f.deleted = false")
    Integer getMaxDisplayOrder();


    Optional<Faq> findByDisplayOrderAndDeletedFalse(int displayOrder);

    List<Faq> findAllByDeletedFalseOrderByDisplayOrderAsc();
}
