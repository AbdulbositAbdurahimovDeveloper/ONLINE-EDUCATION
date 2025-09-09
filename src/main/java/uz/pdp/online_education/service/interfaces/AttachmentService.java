package uz.pdp.online_education.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.UploadLink;
import uz.pdp.online_education.payload.content.AttachmentRequestDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;

import java.util.List;

public interface AttachmentService {
    AttachmentDTO read(Long id);

    AttachmentDTO saveTg(List<PhotoSize> photo);

    void saveTgVideoAsync(Video video, Long chatId,Long lessonId);

    AttachmentDTO create(MultipartFile multipartFile);

    AttachmentDTO saveIcon(MultipartFile multipartFile);

    String tempLink(Long id, Integer minute);

    void delete(Long id);

    UploadLink generateUploadLink(String fileName);

    AttachmentContentDTO complete(AttachmentRequestDTO attachmentRequestDTO, User user);

}
