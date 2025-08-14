package uz.pdp.online_education.service; // Sizdagi paket nomi

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.repository.AttachmentRepository;

import java.util.Comparator;
// Kerakli telegram importlari
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import uz.pdp.online_education.service.interfaces.AttachmentService;

@Slf4j
@Service
public class TelegramUploadTaskServiceImpl implements TelegramUploadTaskService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;

    public TelegramUploadTaskServiceImpl(@Lazy AttachmentRepository attachmentRepository, @Lazy AttachmentService attachmentService) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
    }

    @Async
    @Override
    public void uploadAttachmentToTelegram(Long attachmentId) {
//        log.info("Starting async task to upload attachment {} to Telegram.", attachmentId);
//
//        try {
//            // Bu metod chaqirilganda, bazada attachment allaqachon mavjud bo'ladi
//            Attachment attachment = attachmentRepository.findById(attachmentId)
//                    .orElseThrow(() -> new EntityNotFoundException("Attachment not found for async task: " + attachmentId));
//
//            // Agar allaqachon yuklangan bo'lsa (biror sabab bilan qayta chaqirilsa), o'tkazib yuboramiz
//            if (attachment.getTelegramFileId() != null) {
//                log.warn("Attachment {} already has a Telegram file_id. Skipping async task.", attachmentId);
//                return;
//            }
//
//            // MinIO'dan faylni o'qib olish (bu metod AttachmentService'da public bo'lishi kerak)
////            byte[] fileBytes = attachmentService.downloadById(attachmentId);
//
//            // Telegram kanaliga yuborish (bu metod ham AttachmentService'da public bo'lishi kerak)
////            Message sentMessage = attachmentService.uploadToTelegramChannel(
////                    fileBytes, attachment.getOriginalName(), attachment.getContentType()
//            );
//
//            // file_id'ni olish
//            String fileId = getFileIdFromMessage(sentMessage);
//
//            // Attachment yozuvini yangilash
//            if (fileId != null) {
//                attachment.setTelegramFileId(fileId);
//                attachmentRepository.save(attachment);
//                log.info("Async task completed: Successfully uploaded attachment {} to Telegram. File ID: {}", attachmentId, fileId);
//            }
//
//        } catch (Exception e) {
//            log.error("Async task failed for attachment ID: {}. Error: {}", attachmentId, e.getMessage(), e);
//            e.printStackTrace();
//            // TODO: Xatolikni qayta ishlash logikasi. Masalan, bazaga 'FAILED' statusini yozib qo'yish.
//        }
    }

    // getFileIdFromMessage metodini shu yerga ham ko'chirish
    private String getFileIdFromMessage(Message message) {
        if (message.hasPhoto()) {
            return message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getFileSize)).get().getFileId();
        } else if (message.hasVideo()) {
            return message.getVideo().getFileId();
        } else if (message.hasDocument()) {
            return message.getDocument().getFileId();
        }
        return null;
    }
}