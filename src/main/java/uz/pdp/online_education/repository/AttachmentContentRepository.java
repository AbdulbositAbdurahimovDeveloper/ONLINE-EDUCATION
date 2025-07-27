package uz.pdp.online_education.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.AttachmentContent;

import java.util.List;
import java.util.Optional;

public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, Long> {

    /**
     * Berilgan attachment ID'si orqali u bog'langan kontent bloki, dars va
     * modulni topib, o'sha modulni qaytaradi.
     *
     * @param attachmentId Qidirilayotgan attachment'ning ID'si
     * @return Topilgan modul (Optional ichida) yoki bo'sh Optional
     */
    @Query("""
                SELECT ac.lesson.module
                FROM AttachmentContent ac
                WHERE ac.attachment.id = :attachmentId
            """)
    Optional<Module> findModuleByAttachmentId(@Param("attachmentId") Long attachmentId);


}