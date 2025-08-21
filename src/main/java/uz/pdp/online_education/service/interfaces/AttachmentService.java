package uz.pdp.online_education.service.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;

import java.util.List;

public interface AttachmentService {
    AttachmentDTO read(Long id);

    AttachmentDTO saveTg(List<PhotoSize> photo);

    AttachmentDTO create(MultipartFile multipartFile);

    AttachmentDTO saveIcon(MultipartFile multipartFile);

    String tempLink(Long id, Integer minute);

    void delete(Long id);
}
