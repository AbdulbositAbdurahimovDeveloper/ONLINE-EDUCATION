package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.payload.attachment.AttachmentContentSummaryDTO;
import uz.pdp.online_education.payload.content.ContentDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizContentSummaryDTO;
import uz.pdp.online_education.payload.text.TextContentSummaryDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    // Asosiy o'girish metodi
    // ======================
    @Mapping(source = "module.id", target = "moduleId")
    @Mapping(source = "contents", target = "contents") // Endi bu yerda qualifiedByName shart emas, chunki List<Content> uchun alohida metod yo'q
    LessonResponseDTO toDTO(Lesson lesson);

    // Polimorfik metodlar
    // ===================
    // Bu metod List<Content> ni aylanib chiqib, har birini to'g'ri DTO'ga o'giradi.
    default List<ContentDTO> contentsToContentDTOs(List<Content> contents) {
        if (contents == null) {
            return Collections.emptyList();
        }
        return contents.stream()
                .map(this::contentToContentDTO)
                .collect(Collectors.toList());
    }

    // Bu asosiy "yo'naltiruvchi" (router) metod
    default ContentDTO contentToContentDTO(Content content) {
        if (content instanceof TextContent tc) {
            // TextContent'ni o'girish uchun maxsus metodni chaqiramiz
            return toTextContentDTO(tc);
        } else if (content instanceof AttachmentContent ac) {
            return toAttachmentContentDTO(ac);
        } else if (content instanceof QuizContent qc) {
            return toQuizContentDTO(qc);
        }
        throw new IllegalArgumentException("Unknown Content type: " + content.getClass().getSimpleName());
    }

    // Har bir tur uchun MAXSUS o'girish metodlari
    // ===============================================

    // TextContent -> TextContentDTO
    // HECH QANDAY ANNOTATSIYASIZ! Biz buni default metodda o'zimiz bajaramiz.
    // Bu yerda @Named orqali maxsus metod yozishimiz kerak
    @Named("mapTextContent")
    default TextContentSummaryDTO toTextContentDTO(TextContent textContent) {
        if (textContent == null) {
            return null;
        }
        // Biz qo'lda, lekin aniq qilib DTO yaratamiz
        return new TextContentSummaryDTO(
                textContent.getId(),
                textContent.getBlockOrder(),
                "TEXT", // contentType'ni qo'lda yozamiz
                textContent.getText() // text maydonini to'g'ridan-to'g'ri olamiz
        );
    }


    // AttachmentContent -> AttachmentContentDTO
    @Mapping(target = "contentType", expression = "java(\"ATTACHMENT\")")
    @Mapping(source = "attachment.id", target = "attachmentId")
    @Mapping(source = "attachment.originalName", target = "attachmentOriginalName")
    AttachmentContentSummaryDTO toAttachmentContentDTO(AttachmentContent attachmentContent);


    // QuizContent -> QuizContentDTO
    @Mapping(target = "contentType", expression = "java(\"QUIZ\")")
    @Mapping(source = "quiz.id", target = "quizId")
    @Mapping(source = "quiz.title", target = "quizTitle")
    QuizContentSummaryDTO toQuizContentDTO(QuizContent quizContent);

}