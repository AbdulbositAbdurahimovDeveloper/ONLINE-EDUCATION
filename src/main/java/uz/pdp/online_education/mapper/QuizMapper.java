package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.payload.quiz.QuizResponseDTO;

@Mapper(componentModel = "spring", uses = QuestionMapper.class)
public interface QuizMapper {

    QuizResponseDTO toDto(Quiz quiz);
}
