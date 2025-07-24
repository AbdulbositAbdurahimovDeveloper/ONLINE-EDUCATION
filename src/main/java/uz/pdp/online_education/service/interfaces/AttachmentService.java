package uz.pdp.online_education.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.payload.AttachmentDTO;

public interface AttachmentService {
    AttachmentDTO read(Long id);

    AttachmentDTO create(MultipartFile multipartFile);

    AttachmentDTO saveIcon(MultipartFile multipartFile);

    Resource loadIconAsResource(String filename);

    void delete(Long id);
}
