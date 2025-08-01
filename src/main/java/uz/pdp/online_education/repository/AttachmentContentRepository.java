package uz.pdp.online_education.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    /**
     * Berilgan dars (lesson) uchun o'chirilgan elementning tartibidan katta bo'lgan
     * barcha elementlarning tartibini (blockOrder) bittaga kamaytiradi.
     * @Modifying bu so'rov SELECT emas, balki UPDATE, DELETE yoki INSERT ekanligini bildiradi.
     */
    @Modifying
    @Query("UPDATE AttachmentContent ac SET ac.blockOrder = ac.blockOrder - 1 WHERE ac.lesson.id = :lessonId AND ac.blockOrder > :deletedOrder")
    void decrementBlockOrderAfterDeletion(@Param("lessonId") Long lessonId, @Param("deletedOrder") int deletedOrder);
}