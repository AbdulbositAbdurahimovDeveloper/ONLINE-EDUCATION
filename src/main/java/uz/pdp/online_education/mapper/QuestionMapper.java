package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;

@Mapper(componentModel = "spring",uses = {AttachmentMapper.class})
public interface QuestionMapper {

    QuestionResponseDTO toDto(Question question);
}
