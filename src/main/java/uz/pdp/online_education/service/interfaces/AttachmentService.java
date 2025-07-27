package uz.pdp.online_education.service.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;

public interface AttachmentService {
    AttachmentDTO read(Long id);

    AttachmentDTO create(MultipartFile multipartFile);

    AttachmentDTO saveIcon(MultipartFile multipartFile);

    String tempLink(Long id, Integer minute);

    void delete(Long id);
}
