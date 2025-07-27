package uz.pdp.online_education.service.interfaces;

import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentContentCreateDTO;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;

public interface AttachmentContentService {
    AttachmentContentDTO uploadFile(Long id, User currentUser);

    AttachmentContentDTO create(AttachmentContentCreateDTO attachmentContentCreateDTO);

    void delete(Long id);
}
