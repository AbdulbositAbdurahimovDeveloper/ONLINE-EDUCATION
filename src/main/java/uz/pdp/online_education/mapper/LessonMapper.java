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

//@Mapper(componentModel = "spring")
public interface LessonMapper {
    LessonResponseDTO toDTO(Lesson lesson);

}