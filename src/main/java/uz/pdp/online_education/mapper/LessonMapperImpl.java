package uz.pdp.online_education.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.payload.attachment.AttachmentContentSummaryDTO;
import uz.pdp.online_education.payload.content.ContentDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;
import uz.pdp.online_education.service.interfaces.AttachmentService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LessonMapperImpl implements LessonMapper {

    private final TextContentMapper textContentMapper;
    private final AttachmentContentMapper attachmentContentMapper;
    private final AttachmentService attachmentService;

    @Override
    public LessonResponseDTO toDTO(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getOrderIndex(),
                lesson.isFree(),
                lesson.getModule() != null ? lesson.getModule().getId() : null,
                router(lesson.getContents())
        );
    }

    private List<ContentDTO> router(List<Content> contents) {
        if (contents.isEmpty()) {
            return new ArrayList<>();
        }
        List<ContentDTO> content = new ArrayList<>();

        for (Content oneContent : contents) {
            if (oneContent instanceof AttachmentContent attachmentContent) {
                AttachmentContentSummaryDTO contentDTO = attachmentContentMapper.toAttachmentContentDTO(attachmentContent);
                contentDTO.setAttachmentUrl(attachmentService.tempLink(attachmentContent.getAttachment().getId(), 5));
                content.add(contentDTO);
            } else if (oneContent instanceof TextContent textContent) {
                TextContentResponseDTO dto = textContentMapper.toDTO(textContent);
                content.add(dto);
            } else if (oneContent instanceof QuizContent quizContent) {
                return null;
            }
        }
        return content;
    }

}
