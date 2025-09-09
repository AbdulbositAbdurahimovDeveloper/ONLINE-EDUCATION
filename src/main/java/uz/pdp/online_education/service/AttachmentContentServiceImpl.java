package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.exceptions.UnauthorizedAccessException;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.lesson.AttachmentContent;
import uz.pdp.online_education.mapper.AttachmentContentMapper;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentContentCreateDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;
import uz.pdp.online_education.repository.AttachmentContentRepository;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.service.interfaces.AttachmentContentService;
import uz.pdp.online_education.service.interfaces.AttachmentService;

import java.util.Objects;
import java.util.zip.DataFormatException;

@Service
@RequiredArgsConstructor
public class AttachmentContentServiceImpl implements AttachmentContentService {

    private final AttachmentContentRepository attachmentContentRepository;
    private final AttachmentContentMapper attachmentContentMapper;
    private final LessonRepository lessonRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final TelegramUploadTaskService telegramUploadTaskService;

    @Override
    public AttachmentContentDTO uploadFile(Long id, User currentUser) {
        AttachmentContent attachmentContent = attachmentContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found!"));

        return attachmentContentMapper.toDTO(attachmentContent);
    }

    @Override
    @Transactional
    public AttachmentContentDTO create(AttachmentContentCreateDTO attachmentContentCreateDTO) {
        Lesson lesson = lessonRepository.findById(attachmentContentCreateDTO.getLessonId())
                .orElseThrow(() -> new EntityNotFoundException("Lesson id not found!"));

        Attachment attachment = attachmentRepository.findById(attachmentContentCreateDTO.getAttachmentId())
                .orElseThrow(() -> new EntityNotFoundException("Attachment id not found!"));

        AttachmentContent attachmentContent = new AttachmentContent();
        attachmentContent.setLesson(lesson);
        attachmentContent.setAttachment(attachment);
        attachmentContent.setBlockOrder(lesson.getContents().size());

        attachmentContentRepository.save(attachmentContent);
        return attachmentContentMapper.toDTO(attachmentContent);
    }

    /**
     * AttachmentContent'ni ID bo'yicha o'chiradi va qolgan elementlarning
     * blockOrder'ini qayta tartiblaydi.
     *
     * @param id o'chiriladigan AttachmentContent ID'si
     */
    @Transactional
    @Override
    public void delete(Long id) {
        AttachmentContent attachmentContentToDelete = attachmentContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AttachmentContent not found with id: " + id));

        Long lessonId = attachmentContentToDelete.getLesson().getId();
        int deletedOrder = attachmentContentToDelete.getBlockOrder();

        attachmentContentRepository.delete(attachmentContentToDelete);
        attachmentContentRepository.decrementBlockOrderAfterDeletion(lessonId, deletedOrder);
    }

    /**
     * @param id
     * @param file
     * @param user
     * @return
     */
    @Override
    public AttachmentContentDTO upload(Long id, MultipartFile file, User user) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson id not found!"));

        Course course = lesson.getModule().getCourse();

        if (!Objects.equals(course.getInstructor().getId(), user.getId())) {
            throw new DataConflictException("Lesson sizga tegishli emas");
        }

        AttachmentDTO attachmentDTO = attachmentService.create(file);

        Attachment attachment = attachmentRepository.findById(attachmentDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Attachment id not found!"));

        AttachmentContent attachmentContent = new AttachmentContent();
        attachmentContent.setLesson(lesson);
        attachmentContent.setAttachment(attachment);
        attachmentContent.setBlockOrder(lesson.getContents().size());

        attachmentContentRepository.save(attachmentContent);

        telegramUploadTaskService.uploadAttachmentToTelegram(attachment.getId());
        return attachmentContentMapper.toDTO(attachmentContent);


    }
}
