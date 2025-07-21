package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Long id(Long id);
}