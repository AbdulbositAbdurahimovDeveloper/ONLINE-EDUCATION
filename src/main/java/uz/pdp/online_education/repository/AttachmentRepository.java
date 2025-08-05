package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Attachment;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Long id(Long id);

    List<Attachment> findByMinioKey(String minioKey);

    boolean existsByMinioKey(String minioKey);
}