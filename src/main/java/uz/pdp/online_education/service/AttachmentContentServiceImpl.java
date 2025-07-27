package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.lesson.AttachmentContent;
import uz.pdp.online_education.mapper.AttachmentContentMapper;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;
import uz.pdp.online_education.repository.AttachmentContentRepository;
import uz.pdp.online_education.service.interfaces.AttachmentContentService;

@Service
@RequiredArgsConstructor
public class AttachmentContentServiceImpl implements AttachmentContentService {

    private final AttachmentContentRepository attachmentContentRepository;
    private final AttachmentContentMapper attachmentContentMapper;

    @Override
    public AttachmentContentDTO uploadFile(Long id, User currentUser) {

        AttachmentContent attachmentContent = attachmentContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found!"));



        return attachmentContentMapper.toDTO(attachmentContent);
    }
}
