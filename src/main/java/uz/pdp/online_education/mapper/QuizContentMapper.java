package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.online_education.model.lesson.QuizContent;
import uz.pdp.online_education.payload.quiz.QuizContentResponseDTO;

@Mapper(componentModel = "spring",uses = QuizMapper.class)
public interface QuizContentMapper {

    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(target = "contentType", expression = "java(\"QUIZ\")")
    QuizContentResponseDTO toDto(QuizContent quizContent);
}
