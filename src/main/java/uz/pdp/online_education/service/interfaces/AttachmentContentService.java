package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;

public interface AttachmentContentService {
    AttachmentContentDTO uploadFile(Long id, User currentUser);
}
