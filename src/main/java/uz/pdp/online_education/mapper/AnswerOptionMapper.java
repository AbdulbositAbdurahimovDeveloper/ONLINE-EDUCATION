package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;

@Mapper(componentModel = "spring")
public interface AnswerOptionMapper {

    AnswerOptionResponseDTO toDto(AnswerOption answerOption);

}
